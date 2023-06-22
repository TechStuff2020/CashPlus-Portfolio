package com.portfolio.doctor.payload;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PortfolioRes {
    private List<PortfolioValueRes> responseList = new ArrayList<>();

    private Double feesPaid;
    private Double gainTaxValue;

    private Double standardDeviation;

    public void makeScaled(double scaleValue) {
        feesPaid *= scaleValue;
        standardDeviation *=scaleValue;
        gainTaxValue *=scaleValue;
    }


}
