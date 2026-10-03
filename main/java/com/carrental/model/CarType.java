package com.carrental.model;

import java.math.BigDecimal;

public class CarType {

    private int carTypeId;
    private String typeName;
    private String description;
    private BigDecimal dailyRate;

    public CarType(int carTypeId, String typeName, String description, BigDecimal dailyRate) {
        this.carTypeId = carTypeId;
        this.typeName = typeName;
        this.description = description;
        this.dailyRate = dailyRate;
    }

    public int getCarTypeId() { return carTypeId; }
    public String getTypeName() { return typeName; }
    public String getDescription() { return description; }
    public BigDecimal getDailyRate() { return dailyRate; }
}
