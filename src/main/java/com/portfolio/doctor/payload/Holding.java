package com.portfolio.doctor.payload;

import lombok.Data;

@Data
public class Holding extends HoldingScaled{
    private int quantity;

    public Holding(String ticker,int quantity, double value) {
        super(ticker, value);
        this.quantity=quantity;
    }



}
