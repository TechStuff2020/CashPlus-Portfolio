package com.portfolio.doctor.payload;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Currency;
import java.util.List;

@Data
public class TradeDto {
    @Valid
    @NotNull
    @NotEmpty
    private List<Trade> tradeList;

    private Currency currency;

    private boolean scaleOutput;

    private String benchmarkSymbol;

    @Min(value = 0, message = "return rate should be positive")
    private double cashReturn;

    @Min(value = 0, message = "tax rate should be positive")
    private double gainTax;


}
