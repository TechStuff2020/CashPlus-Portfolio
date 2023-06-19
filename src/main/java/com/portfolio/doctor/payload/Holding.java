package com.portfolio.doctor.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@Data
@AllArgsConstructor
public class Holding {
    private String ticker;
    private int quantity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Holding holding = (Holding) o;
        return Objects.equals(ticker, holding.ticker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticker);
    }
}
