package com.portfolio.doctor.service;

import com.portfolio.doctor.payload.ApiResponse;
import com.portfolio.doctor.payload.Holding;
import com.portfolio.doctor.payload.PortfolioValueRes;
import com.portfolio.doctor.payload.Trade;
import com.portfolio.doctor.payload.TradeDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PortfolioServiceImpl implements PortfolioService {
    private final WebClient webClient;

    private Map<String, String> companyCurrencyMap = new HashMap<>();

    public PortfolioServiceImpl() {
        this.webClient = WebClient.create();
    }

    @Override
    public ResponseEntity<ApiResponse> processTrades(@RequestBody TradeDto tradeDto) {
        TreeMap<LocalDate, ArrayList<Trade>> tradesGroupedByTheirDate = groupTradesByTheirDate(tradeDto.getTradeList());
        List<PortfolioValueRes> responseList = new ArrayList<>();
        calculateResult(tradesGroupedByTheirDate, responseList, tradeDto);
        return ResponseEntity.ok(new ApiResponse(true, responseList));

    }

    private void calculateResult(TreeMap<LocalDate, ArrayList<Trade>> tradesGroupedByTheirDate, List<PortfolioValueRes> responseList, TradeDto tradeDto) {
        CashNetHolder cashNetHolder = new CashNetHolder();
        Map<String, TreeMap<LocalDate, Map<String, String>>> tickerGroupTree = new HashMap<>();
        Map<String, TreeMap<LocalDate, Map<String, String>>> currExchangeRateMap = new HashMap<>();
        Map<String, Holding> holdingMap = new HashMap<>();

        //getting first trade date
        LocalDate tradeDate = tradesGroupedByTheirDate.firstEntry().getKey().plusDays(0);
        LocalDate currentDate = LocalDate.now();

        while (tradeDate.compareTo(currentDate) <= 0) {

            handleTrades(tradesGroupedByTheirDate, cashNetHolder, tickerGroupTree, holdingMap, tradeDate, tradeDto, currExchangeRateMap);

            PortfolioValueRes portfolioValueRes = createPortfolio(cashNetHolder, holdingMap, tradeDate);

            calculateValues(tickerGroupTree, tradeDate, portfolioValueRes, currExchangeRateMap, tradeDto);

            responseList.add(portfolioValueRes);

            tradeDate = tradeDate.plusDays(7);
        }
    }

    private PortfolioValueRes createPortfolio(CashNetHolder cashNetHolder, Map<String, Holding> holdingMap, LocalDate tradeDate) {
        PortfolioValueRes portfolioValueRes = createTestRes(tradeDate);
        portfolioValueRes.setHoldingList(new HashSet<>(holdingMap.values().stream()
                .map(h -> new Holding(h.getTicker(), h.getQuantity()))
                .collect(Collectors.toSet())));
        portfolioValueRes.setNetInvestment(cashNetHolder.net);
        portfolioValueRes.setPortfolioCash(cashNetHolder.cash);
        return portfolioValueRes;
    }

    private void handleTrades(TreeMap<LocalDate, ArrayList<Trade>> tradesGroupedByTheirDate,
                              CashNetHolder cashNetHolder,
                              Map<String, TreeMap<LocalDate, Map<String, String>>> tickerGroupTree,
                              Map<String, Holding> holdingMap,
                              LocalDate tradeDate,
                              TradeDto tradeDto,
                              Map<String, TreeMap<LocalDate, Map<String, String>>> currExchangeRateMap) {
        ArrayList<Trade> trades = tradesGroupedByTheirDate.getOrDefault(tradeDate, new ArrayList<>());
        cashNetHolder.cash += (tradeDto.getCashReturn() * cashNetHolder.cash / 5200);
        for (Trade trade : trades) {
            checkCompanyCurrency(trade);

            Holding holding = holdingMap.computeIfAbsent(trade.getTicker(), i -> new Holding(i, 0));
            holding.setQuantity(holding.getQuantity() + (trade.getAction() * trade.getQuantity()));

            var tickData =
                    getWeeklyTimeSeriesDataByTickerName(
                            tickerGroupTree, trade.getTicker()
                    );
            var currencyData =
                    getWeeklySeriesFXDataByCurrencyCode(
                            currExchangeRateMap,
                            companyCurrencyMap.get(trade.getTicker()), tradeDto.getCurrency().getCurrencyCode()
                    );
            Map<String, String> tradeObjectOfTradeDate = findResultOrReturnClosestObject(tickData, tradeDate);
            Map<String, String> currExchangeObj = findResultOrReturnClosestObject(currencyData, tradeDate);
            double closePrice = Double.parseDouble(tradeObjectOfTradeDate.get("4. close")) * Double.parseDouble(currExchangeObj.get("4. close"));

            cashNetHolder.cash -= calculateTradeCost(trade, closePrice);
            cashNetHolder.net += Math.max(-cashNetHolder.cash, 0);
            cashNetHolder.cash = Math.max(cashNetHolder.cash, 0);

        }
    }

    private static Map<String, String> findResultOrReturnClosestObject(TreeMap<LocalDate, Map<String, String>> tickData, LocalDate tradeDate) {
        Map<String, String> stringStringMap = !tickData.containsKey(tradeDate) ? tickData.floorEntry(tradeDate).getValue() : tickData.get(tradeDate);
        return stringStringMap;
    }

    private void checkCompanyCurrency(Trade trade) {
        if (!companyCurrencyMap.containsKey(trade.getTicker())) {
            fetchCompanyReport(trade.getTicker());
        }
    }

    private double calculateTradeCost(Trade trade, double closePrice) {
        closePrice = trade.getPrice() == null ? closePrice : trade.getPrice();
        double costWithoutFees = trade.getAction() * trade.getQuantity() * closePrice;
        double fees = trade.getFixedFee() + (trade.getQuantity() * closePrice * trade.getVariableFee() / 100);
        return costWithoutFees + fees;
    }

    private PortfolioValueRes createTestRes(LocalDate date) {
        PortfolioValueRes portfolioValueRes = new PortfolioValueRes();
        portfolioValueRes.setDate(date);
        return portfolioValueRes;
    }

    private void calculateValues(Map<String, TreeMap<LocalDate, Map<String, String>>> tickerList, LocalDate tradeDate, PortfolioValueRes portfolioValueRes1, Map<String, TreeMap<LocalDate, Map<String, String>>> currExchangeRateMap, TradeDto tradeDto) {
        for (Holding holding : portfolioValueRes1.getHoldingList()) {
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
                    holding.getQuantity());
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
        String apiUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_WEEKLY&symbol=" + ticker + "&apikey=AETILB69JPD6DTUK";
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

    private String fetchCompanyReport(String sym) {
        String apiUrl = "https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=" + sym + "&apikey=AETILB69JPD6DTUK";
        JsonNode jsonResponse = webClient.get()
                .uri(apiUrl)
                .retrieve()
                .bodyToFlux(JsonNode.class)
                .blockFirst();

        assert jsonResponse != null;
        return getCurrencyAndPutIntoMap(sym, jsonResponse);

    }

    private TreeMap<LocalDate, Map<String, String>> fetchWeeklyFXexchangeRate(String toCurr, String fromCurr, Map<String, TreeMap<LocalDate, Map<String, String>>> currList) {
        String apiUrl = "https://www.alphavantage.co/query?function=FX_WEEKLY&from_symbol=" + fromCurr + "&to_symbol=" + toCurr + "&apikey=AETILB69JPD6DTUK";
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

    private String getCurrencyAndPutIntoMap(String sym, JsonNode jsonResponse) {
        JsonNode bestMatchesNode = jsonResponse.get("bestMatches");

        if (bestMatchesNode.isArray()) {
            for (JsonNode matchNode : bestMatchesNode) {
                if (matchNode.get("1. symbol").textValue().equals(sym)) {
                    companyCurrencyMap.put(sym, matchNode.get("8. currency").textValue());
                }
            }
        }
        return companyCurrencyMap.get(sym);
    }

    private static TreeMap<LocalDate, Map<String, String>> convertWeeklyTimeSeriesJsonToTreeMapAndReturn(JsonNode jsonResponse, String targetKey) {
        // Convert the "Weekly Time Series" data into a TreeMap
        TreeMap<LocalDate, Map<String, String>> weeklyTimeSeriesMap = new TreeMap<>();
        JsonNode weeklyTimeSeries = jsonResponse.get(targetKey);
        ObjectMapper objectMapper = new ObjectMapper();
        if (weeklyTimeSeries == null) {
            System.out.println(weeklyTimeSeries);
        }
        weeklyTimeSeries.fields().forEachRemaining(entry -> {
            LocalDate date = LocalDate.parse(entry.getKey());
            JsonNode data = entry.getValue();
            Map<String, String> weeklyTimeSeriesConverted = objectMapper.convertValue(data, new TypeReference<>() {
            });

            weeklyTimeSeriesMap.put(date, weeklyTimeSeriesConverted);
        });
        return weeklyTimeSeriesMap;
    }

    @Getter
    @Setter
    static class CashNetHolder {
        double cash = 0;
        double net = 0;
    }
}
