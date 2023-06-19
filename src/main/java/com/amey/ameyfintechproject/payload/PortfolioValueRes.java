package com.amey.ameyfintechproject.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioValueRes {
    private Double value=0.0;
    private Set<Holding> holdingList=new HashSet<>();
    private Double portfolioCash;
    private Double netInvestment;
    private LocalDate date;
}
