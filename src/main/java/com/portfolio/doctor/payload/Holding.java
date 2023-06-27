package com.portfolio.doctor.payload;

import lombok.Data;

import java.util.Objects;

@Data
public class Holding extends HoldingScaled{
    private int quantity;

    public Holding(String ticker,int quantity, double value) {
        super(ticker, value);
        this.quantity=quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HoldingScaled holding)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(super.getTicker(), holding.getTicker());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), super.getTicker());
    }
}
