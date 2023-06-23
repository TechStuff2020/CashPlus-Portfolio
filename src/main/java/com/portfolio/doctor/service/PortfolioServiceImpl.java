package com.portfolio.doctor.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.portfolio.doctor.payload.*;
import com.portfolio.doctor.util.ApplicationProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
@Service
public class PortfolioServiceImpl implements PortfolioService {
    private final WebClient webClient;

    private final Map<String, String> companyCurrencyMap = new HashMap<>();

    private final ApplicationProperties applicationProperties;

    @Autowired
    public PortfolioServiceImpl(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.webClient = WebClient.create();

    }

    @Override
    public ResponseEntity<ApiResponse> processTrades(@RequestBody TradeDto tradeDto) {
        TreeMap<LocalDate, ArrayList<Trade>> tradesGroupedByTheirDate = groupTradesByTheirDate(tradeDto.getTradeList());

        return ResponseEntity.ok(new ApiResponse(true, calculateResult(tradesGroupedByTheirDate, tradeDto)));

    }

    private PortfolioRes calculateResult(TreeMap<LocalDate, ArrayList<Trade>> tradesGroupedByTheirDate, TradeDto tradeDto) {
        PortfolioRes portfolioRes = new PortfolioRes();
        CashNetHolder cashNetHolder = new CashNetHolder();
        Map<String, TreeMap<LocalDate, Map<String, String>>> tickerGroupTree = new HashMap<>();
        Map<String, TreeMap<LocalDate, Map<String, String>>> currExchangeRateMap = new HashMap<>();
        Map<String, Holding> holdingMap = new HashMap<>();

        _calculateResult(tradesGroupedByTheirDate, tradeDto, portfolioRes, cashNetHolder, tickerGroupTree, currExchangeRateMap, holdingMap);

        cashNetHolder.setCash(portfolioRes.getResponseList().get(portfolioRes.getResponseList().size() - 1).getNetNewPurchase());
        cashNetHolder.setNet(portfolioRes.getResponseList().get(0).getNetNewPurchase());
        cashNetHolder.setFees(0);
        cashNetHolder.setTaxes(0);
        portfolioRes = new PortfolioRes();
        holdingMap = new HashMap<>();
        _calculateResult(tradesGroupedByTheirDate, tradeDto, portfolioRes, cashNetHolder, tickerGroupTree, currExchangeRateMap, holdingMap);
        checkScaled(tradeDto.isScaleOutput(), portfolioRes);
        return portfolioRes;
    }

    private void _calculateResult(TreeMap<LocalDate, ArrayList<Trade>> tradesGroupedByTheirDate, TradeDto tradeDto,
                                  PortfolioRes portfolioRes, CashNetHolder cashNetHolder,
                                  Map<String, TreeMap<LocalDate, Map<String, String>>> tickerGroupTree,
                                  Map<String, TreeMap<LocalDate, Map<String, String>>> currExchangeRateMap,
                                  Map<String, Holding> holdingMap) {
        //getting first trade date
        LocalDate tradeDate = tradesGroupedByTheirDate.firstEntry().getKey().plusDays(0);
        LocalDate currentDate = LocalDate.now();
        List<Gain> gains = new ArrayList<>();
        while (!tradeDate.isAfter(currentDate)) {

            handleTrades(tradesGroupedByTheirDate, cashNetHolder, tickerGroupTree, holdingMap, tradeDate, tradeDto, currExchangeRateMap, gains);

            PortfolioValueRes portfolioValueRes = createPortfolio(cashNetHolder, holdingMap, tradeDate);

            calculateValues(tickerGroupTree, tradeDate, portfolioValueRes, currExchangeRateMap, tradeDto);

            portfolioRes.getResponseList().add(portfolioValueRes);

            cashNetHolder.cash += (tradeDto.getCashReturn() * cashNetHolder.cash / 5200);

            portfolioValueRes.setGains(gains.stream().map(Gain::clone).toList());

            tradeDate = tradeDate.plusDays(7);
        }
        handleStandardDeviationAndInitialCash(portfolioRes);
        portfolioRes.setFeesPaid(cashNetHolder.fees);
        portfolioRes.setGainTaxValue(cashNetHolder.taxes);
    }

    private void handleStandardDeviationAndInitialCash(PortfolioRes portfolioRes) {
        //create an empty array for standard  deviation logic
        List<Double> standardDeviationList = new ArrayList<>();

        double sum = getSumOfTotalValuesAndHandleInitialCash(portfolioRes, standardDeviationList);

        // get the mean of array
        int length = standardDeviationList.size();
        double mean = sum / length;

        // calculate the standard deviation
        double standardDeviation = 0.0;
        for (double num : standardDeviationList) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        portfolioRes.setStandardDeviation(Math.sqrt(standardDeviation / length));
    }

    private static double getSumOfTotalValuesAndHandleInitialCash(PortfolioRes portfolioRes, List<Double> standardDeviationList) {
        double sum = 0.0;
        for (PortfolioValueRes portfolioValueRes : portfolioRes.getResponseList()) {

            double total = portfolioValueRes.getValue() + portfolioValueRes.getPortfolioCash();
            standardDeviationList.add(total);
            sum += total;


        }
        return sum;
    }


    private void checkScaled(boolean scaleOutput, PortfolioRes portfolioRes) {
        if (scaleOutput) {
            double scaleValue = 100 / portfolioRes.getResponseList().get(portfolioRes.getResponseList().size() - 1).getNetNewPurchase();
            portfolioRes.makeScaled(scaleValue);
            portfolioRes.setResponseList(portfolioRes.getResponseList().stream().peek(i -> {
                i.makeScaled(scaleValue);
                for (Gain gain : i.getGains()) {
                    gain.makeScaled(scaleValue);
                }
                i.setHoldingList(i.getHoldingList().stream().map(j -> {
                    j.makeScaled(scaleValue);
                    return new HoldingScaled(j.getTicker(), j.getPositionValue());
                }).collect(Collectors.toSet()));
            }).toList());
        }
    }

    private PortfolioValueRes createPortfolio(CashNetHolder cashNetHolder, Map<String, Holding> holdingMap, LocalDate tradeDate) {
        PortfolioValueRes portfolioValueRes = createTestRes(tradeDate);
        portfolioValueRes.setHoldingList(new HashSet<>(holdingMap.values().stream()
                .map(h -> new Holding(h.getTicker(), h.getQuantity(), 0))
                .collect(Collectors.toSet())));
        portfolioValueRes.setNetNewPurchase(cashNetHolder.net);
        portfolioValueRes.setPortfolioCash(cashNetHolder.cash);
        return portfolioValueRes;
    }

    private void handleTrades(TreeMap<LocalDate, ArrayList<Trade>> tradesGroupedByTheirDate,
                              CashNetHolder cashNetHolder,
                              Map<String, TreeMap<LocalDate, Map<String, String>>> tickerGroupTree,
                              Map<String, Holding> holdingMap,
                              LocalDate tradeDate,
                              TradeDto tradeDto,
                              Map<String, TreeMap<LocalDate, Map<String, String>>> currExchangeRateMap,
                              List<Gain> gains) {
        ArrayList<Trade> trades = tradesGroupedByTheirDate.getOrDefault(tradeDate, new ArrayList<>());
        for (Trade trade : trades) {
            checkCompanyCurrency(trade);

            Holding holding = holdingMap.computeIfAbsent(trade.getTicker(), i -> new Holding(i, 0, 0));
            holding.setQuantity(holding.getQuantity() + (trade.getAction() * trade.getQuantity()));

            double closePrice = getClosePrice(tickerGroupTree, tradeDate, tradeDto, currExchangeRateMap, trade.getTicker());

            handleGains(cashNetHolder, tradeDto, gains, trade, closePrice);


            cashNetHolder.cash -= calculateTradeCost(trade, closePrice, cashNetHolder);
            cashNetHolder.net += Math.max(-cashNetHolder.cash, 0);
            cashNetHolder.cash = Math.max(cashNetHolder.cash, 0);

        }
        for (Gain gain : gains) {
            double closePrice = getClosePrice(tickerGroupTree, tradeDate, tradeDto, currExchangeRateMap, gain.getTicker());
            gain.setGains((closePrice - gain.getPrice()) * gain.getQuantity());
        }

    }

    private void handleGains(CashNetHolder cashNetHolder, TradeDto tradeDto, List<Gain> gains, Trade trade, double closePrice) {
        if (trade.getAction() == 1) {
            gains.add(new Gain(trade.getTicker(), trade.getQuantity(), gains.size() + 1, closePrice));
        } else {
            int quantity = trade.getQuantity();
            for (Gain gain : gains) {
                if (quantity == 0) break;
                if (!gain.getTicker().equals(trade.getTicker()) || gain.getQuantity() <= 0) continue;
                int gainInitQt = gain.getQuantity();
                gain.setQuantity(Math.max(gain.getQuantity() - quantity, 0));
                double gainValue = (closePrice - gain.getPrice()) * (gainInitQt - gain.getQuantity());
                quantity -= gainInitQt;
                handleGainTax(cashNetHolder, tradeDto, gain, gainValue);
                gain.setBookedGains(gain.getBookedGains() + gainValue);
            }
        }
    }

    private void handleGainTax(CashNetHolder cashNetHolder, TradeDto tradeDto, Gain gain, double gainValue) {
        if (gainValue > 0) {
            double tax = gainValue * tradeDto.getGainTax() / 100;
            cashNetHolder.cash -= tax;
            cashNetHolder.taxes+= tax;
            gain.setTax(gain.getTax() + tax);
        }
    }

    private double getClosePrice(Map<String, TreeMap<LocalDate, Map<String, String>>> tickerGroupTree, LocalDate tradeDate,
                                 TradeDto tradeDto, Map<String, TreeMap<LocalDate, Map<String, String>>> currExchangeRateMap,
                                 String ticker) {
        var tickData =
                getWeeklyTimeSeriesDataByTickerName(
                        tickerGroupTree, ticker
                );
        var currencyData =
                getWeeklySeriesFXDataByCurrencyCode(
                        currExchangeRateMap,
                        companyCurrencyMap.get(ticker), tradeDto.getCurrency().getCurrencyCode()
                );
        return getClosePrice(tradeDate, tickData, currencyData);
    }

    private static double getClosePrice(LocalDate tradeDate, TreeMap<LocalDate, Map<String, String>> tickData, TreeMap<LocalDate, Map<String, String>> currencyData) {
        Map<String, String> tradeObjectOfTradeDate = findResultOrReturnClosestObject(tickData, tradeDate);
        Map<String, String> currExchangeObj = findResultOrReturnClosestObject(currencyData, tradeDate);
        return Double.parseDouble(tradeObjectOfTradeDate.get("4. close")) * Double.parseDouble(currExchangeObj.get("4. close"));
    }


    private static Map<String, String> findResultOrReturnClosestObject(TreeMap<LocalDate, Map<String, String>> tickData, LocalDate tradeDate) {
        return !tickData.containsKey(tradeDate) ? tickData.floorEntry(tradeDate).getValue() : tickData.get(tradeDate);
    }

    private void checkCompanyCurrency(Trade trade) {
        if (!companyCurrencyMap.containsKey(trade.getTicker())) {
            fetchCompanyReport(trade.getTicker());
        }
    }

    private double calculateTradeCost(Trade trade, double closePrice, CashNetHolder cashNetHolder) {
        closePrice = trade.getPrice() == null ? closePrice : trade.getPrice();
        double costWithoutFees = trade.getAction() * trade.getQuantity() * closePrice;
        double fees = trade.getFixedFee() + (trade.getQuantity() * closePrice * trade.getVariableFee() / 100);
        cashNetHolder.fees += fees;
        return costWithoutFees + fees;
    }

    private PortfolioValueRes createTestRes(LocalDate date) {
        PortfolioValueRes portfolioValueRes = new PortfolioValueRes();
        portfolioValueRes.setDate(date);
        return portfolioValueRes;
    }

    private void calculateValues(Map<String, TreeMap<LocalDate, Map<String, String>>> tickerList, LocalDate tradeDate,
                                 PortfolioValueRes portfolioValueRes1,
                                 Map<String, TreeMap<LocalDate, Map<String, String>>> currExchangeRateMap, TradeDto tradeDto) {
        for (HoldingScaled holding : portfolioValueRes1.getHoldingList()) {

            TreeMap<LocalDate, Map<String, String>> tickData = getWeeklyTimeSeriesDataByTickerName(tickerList, holding.getTicker());
            var currencyData =
                    getWeeklySeriesFXDataByCurrencyCode(
                            currExchangeRateMap,
                            companyCurrencyMap.get(holding.getTicker()), tradeDto.getCurrency().getCurrencyCode()
                    );
            Map<String, String> tradeObjOfTradeDate = findResultOrReturnClosestObject(tickData, tradeDate);

            Map<String, String> currExchangeObj = findResultOrReturnClosestObject(currencyData, tradeDate);

            double v = (Double.parseDouble(tradeObjOfTradeDate.get("4. close")) *
                    Double.parseDouble(currExchangeObj.get("4. close")) *
                    ((Holding) holding).getQuantity());
            holding.setPositionValue(v);
            portfolioValueRes1.setValue(portfolioValueRes1.getValue() + v);
        }
    }

    private TreeMap<LocalDate, Map<String, String>> getWeeklyTimeSeriesDataByTickerName(Map<String, TreeMap<LocalDate, Map<String, String>>> tickerList, String ticker) {
        return tickerList.containsKey(ticker) ? tickerList.get(ticker) :
                fetchWeeklyTimeSeries(ticker, tickerList);
    }

    private TreeMap<LocalDate, Map<String, String>> getWeeklySeriesFXDataByCurrencyCode(Map<String, TreeMap<LocalDate, Map<String, String>>> tickerList, String fromCurr, String toCurr) {
        return tickerList.containsKey(fromCurr + "-" + toCurr) ? tickerList.get(fromCurr + "-" + toCurr) :
                fetchWeeklyFXexchangeRate(toCurr, fromCurr, tickerList);
    }

    private static TreeMap<LocalDate, ArrayList<Trade>> groupTradesByTheirDate(List<Trade> d) {
        for (Trade i : d) {
            i.setDate(i.getDate().getDayOfWeek() != DayOfWeek.FRIDAY ? i.getDate().with(DayOfWeek.FRIDAY) : i.getDate());
        }
        return d.stream().sorted(Comparator.comparing(Trade::getDate)).collect(Collectors.toMap(
                Trade::getDate,
                obj -> new ArrayList<>(List.of(obj)),
                (list1, list2) -> {
                    ArrayList<Trade> mergedList = new ArrayList<>();
                    mergedList.addAll(list1);
                    mergedList.addAll(list2);
                    return mergedList;
                },
                TreeMap::new
        ));
    }


    private TreeMap<LocalDate, Map<String, String>> fetchWeeklyTimeSeries(String ticker, Map<String, TreeMap<LocalDate, Map<String, String>>> tickerList) {
        String apiUrl = getWeeklyTimeSeriesUrl(ticker);
        JsonNode jsonResponse = webClient.get()
                .uri(apiUrl)
                .retrieve()
                .bodyToFlux(JsonNode.class)
                .blockFirst();
        assert jsonResponse != null;
        TreeMap<LocalDate, Map<String, String>> weeklyTimeSeries = convertWeeklyTimeSeriesJsonToTreeMapAndReturn(jsonResponse, "Weekly Time Series");

        tickerList.put(ticker, weeklyTimeSeries);

        return weeklyTimeSeries;

    }

    private void fetchCompanyReport(String sym) {
        String apiUrl = getCompanyReportUrl(sym);
        JsonNode jsonResponse = webClient.get()
                .uri(apiUrl)
                .retrieve()
                .bodyToFlux(JsonNode.class)
                .blockFirst();

        assert jsonResponse != null;
        getCurrencyAndPutIntoMap(sym, jsonResponse);

    }

    private TreeMap<LocalDate, Map<String, String>> fetchWeeklyFXexchangeRate(String toCurr, String fromCurr, Map<String, TreeMap<LocalDate, Map<String, String>>> currList) {
        String apiUrl = getWeeklyFXExchangeUrl(toCurr,fromCurr);
        JsonNode jsonResponse = webClient.get()
                .uri(apiUrl)
                .retrieve()
                .bodyToFlux(JsonNode.class)
                .blockFirst();

        assert jsonResponse != null;
        TreeMap<LocalDate, Map<String, String>> weeklySeriesFX = convertWeeklyTimeSeriesJsonToTreeMapAndReturn(jsonResponse, "Time Series FX (Weekly)");

        currList.put(fromCurr + "-" + toCurr, weeklySeriesFX);

        return weeklySeriesFX;
    }

    private void getCurrencyAndPutIntoMap(String sym, JsonNode jsonResponse) {
        JsonNode bestMatchesNode = jsonResponse.get("bestMatches");

        if (bestMatchesNode.isArray()) {
            for (JsonNode matchNode : bestMatchesNode) {
                if (matchNode.get("1. symbol").textValue().equals(sym)) {
                    companyCurrencyMap.put(sym, matchNode.get("8. currency").textValue());
                }
            }
        }
        companyCurrencyMap.get(sym);
    }

    private static TreeMap<LocalDate, Map<String, String>> convertWeeklyTimeSeriesJsonToTreeMapAndReturn(JsonNode jsonResponse, String targetKey) {
        // Convert the "Weekly Time Series" data into a TreeMap
        TreeMap<LocalDate, Map<String, String>> weeklyTimeSeriesMap = new TreeMap<>();
        JsonNode weeklyTimeSeries = jsonResponse.get(targetKey);
        ObjectMapper objectMapper = new ObjectMapper();
        assert weeklyTimeSeries != null;
        weeklyTimeSeries.fields().forEachRemaining(entry -> {
            LocalDate date = LocalDate.parse(entry.getKey());
            JsonNode data = entry.getValue();
            Map<String, String> weeklyTimeSeriesConverted = objectMapper.convertValue(data, new TypeReference<>() {
            });

            weeklyTimeSeriesMap.put(date, weeklyTimeSeriesConverted);
        });
        return weeklyTimeSeriesMap;
    }


    private String getCompanyReportUrl(String sym) {
        return UriComponentsBuilder.fromUriString(applicationProperties.getTickerUrl())
                .path("query")
                .queryParam("function", "SYMBOL_SEARCH")
                .queryParam("keywords", sym)
                .queryParam("apikey", applicationProperties.getKey()).build()
                .toUriString();
    }

    private String getWeeklyTimeSeriesUrl(String ticker) {
        return UriComponentsBuilder.fromUriString(applicationProperties.getTickerUrl())
                .path("query")
                .queryParam("function", "TIME_SERIES_WEEKLY")
                .queryParam("symbol", ticker)
                .queryParam("apikey", applicationProperties.getKey()).build()
                .toUriString();
    }

    private String getWeeklyFXExchangeUrl(String toCurr, String fromCurr) {
        return UriComponentsBuilder.fromUriString(applicationProperties.getTickerUrl())
                .path("query")
                .queryParam("function", "FX_WEEKLY")
                .queryParam("from_symbol", fromCurr)
                .queryParam("to_symbol", toCurr)
                .queryParam("apikey", applicationProperties.getKey()).build()
                .toUriString();
    }


    @Getter
    @Setter
    static class CashNetHolder {
        double cash = 0;
        double net = 0;
        double fees = 0;
        double taxes = 0;
    }
}
