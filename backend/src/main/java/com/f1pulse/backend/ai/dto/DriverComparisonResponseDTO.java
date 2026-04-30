package com.f1pulse.backend.ai.dto;

public class DriverComparisonResponseDTO {

    private String betterDriver;
    private String reason;

    public DriverComparisonResponseDTO() {}

    public DriverComparisonResponseDTO(String betterDriver, String reason) {
        this.betterDriver = betterDriver;
        this.reason = reason;
    }

    public String getBetterDriver() {
        return betterDriver;
    }

    public void setBetterDriver(String betterDriver) {
        this.betterDriver = betterDriver;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
