package com.portfolio.doctor.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HoldingScaled {
    private String ticker;
    private double positionValue;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoldingScaled holding = (HoldingScaled) o;
        return Objects.equals(ticker, holding.ticker);
    }

    public void makeScaled(double scaleValue) {
        positionValue *= scaleValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticker);
    }
}
