package com.portfolio.doctor.payload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Trade {
    //1 means buy and -1 means sell
    private byte action;

    private double quantity;

    @NotBlank
    private String ticker;

    @NotNull
    private LocalDate date;

    @Min(value = 0,message = "fixed fee must be positive")
    private double fixedFee;

    //comes as percent
    @Min(value = 0,message = "variable fee must be positive")
    private double variableFee;

    @Min(value = 0,message = "price must be positive")
    private Double price;

}
