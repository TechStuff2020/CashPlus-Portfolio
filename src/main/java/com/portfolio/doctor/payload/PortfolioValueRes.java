package com.portfolio.doctor.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioValueRes {
    private Double value = 0.0;
    private Set<HoldingScaled> holdingList = new HashSet<>();
    private List<Gain> gains = new ArrayList<>();
    private Double portfolioCash;
    private Double netNewPurchase;
    private LocalDate date;

    public void makeScaled(double scaleValue) {
        value *= scaleValue;
        portfolioCash *= scaleValue;
        netNewPurchase *= scaleValue;
    }
}
