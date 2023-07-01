package com.portfolio.doctor.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data

public class Gain implements Cloneable {

    private String ticker;
    private double quantity;
    private double gains;
    private double tax;
    private int seqNo;
    private double bookedGains;

    @JsonIgnore
    private double price;


    public void makeScaled(double scaleValue) {
        gains *= scaleValue;
        bookedGains *= scaleValue;
        tax *= scaleValue;
        quantity=0;
    }

    public Gain(String ticker, double quantity, int seqNo, double price) {
        this.ticker = ticker;
        this.quantity = quantity;
        this.seqNo = seqNo;
        this.price = price;
    }

    public Gain() {
    }

    @Override
    public Gain clone() {
        try {
            return (Gain) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
