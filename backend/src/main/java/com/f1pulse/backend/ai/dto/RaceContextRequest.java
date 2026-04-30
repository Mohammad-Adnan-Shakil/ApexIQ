package com.f1pulse.backend.ai.dto;

import java.util.Objects;

/**
 * Request payload for Race Engineer AI analysis.
 * Contains real-time race context and driver telemetry.
 */
public class RaceContextRequest {
    
    /**
     * Current lap number (1-indexed)
     */
    private int lap;
    
    /**
     * Total laps in the race/session
     */
    private int totalLaps;
    
    /**
     * Current grid position (1-indexed)
     */
    private int position;
    
    /**
     * Gap to leader as formatted string, e.g. "+12.4s" or "1L"
     */
    private String gapToLeader;
    
    /**
     * Tire compound: SOFT, MEDIUM, HARD, INTER, WET
     */
    private String tyreCompound;
    
    /**
     * Tyre age in completed laps
     */
    private int tyreAge;
    
    /**
     * Remaining fuel load in kilograms
     */
    private double fuelLoad;
    
    /**
     * Current weather condition: Dry, Damp, Wet
     */
    private String weather;
    
    /**
     * Last completed lap time as formatted string, e.g. "1:22.847"
     */
    private String lastLapTime;
    
    /**
     * Driver's message or observation from the car
     */
    private String driverMessage;

    public RaceContextRequest() {
    }

    public RaceContextRequest(int lap, int totalLaps, int position, String gapToLeader, String tyreCompound, int tyreAge, double fuelLoad, String weather, String lastLapTime, String driverMessage) {
        this.lap = lap;
        this.totalLaps = totalLaps;
        this.position = position;
        this.gapToLeader = gapToLeader;
        this.tyreCompound = tyreCompound;
        this.tyreAge = tyreAge;
        this.fuelLoad = fuelLoad;
        this.weather = weather;
        this.lastLapTime = lastLapTime;
        this.driverMessage = driverMessage;
    }

    public int getLap() {
        return lap;
    }

    public void setLap(int lap) {
        this.lap = lap;
    }

    public int getTotalLaps() {
        return totalLaps;
    }

    public void setTotalLaps(int totalLaps) {
        this.totalLaps = totalLaps;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getGapToLeader() {
        return gapToLeader;
    }

    public void setGapToLeader(String gapToLeader) {
        this.gapToLeader = gapToLeader;
    }

    public String getTyreCompound() {
        return tyreCompound;
    }

    public void setTyreCompound(String tyreCompound) {
        this.tyreCompound = tyreCompound;
    }

    public int getTyreAge() {
        return tyreAge;
    }

    public void setTyreAge(int tyreAge) {
        this.tyreAge = tyreAge;
    }

    public double getFuelLoad() {
        return fuelLoad;
    }

    public void setFuelLoad(double fuelLoad) {
        this.fuelLoad = fuelLoad;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getLastLapTime() {
        return lastLapTime;
    }

    public void setLastLapTime(String lastLapTime) {
        this.lastLapTime = lastLapTime;
    }

    public String getDriverMessage() {
        return driverMessage;
    }

    public void setDriverMessage(String driverMessage) {
        this.driverMessage = driverMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RaceContextRequest that = (RaceContextRequest) o;
        return lap == that.lap &&
                totalLaps == that.totalLaps &&
                position == that.position &&
                tyreAge == that.tyreAge &&
                Double.compare(that.fuelLoad, fuelLoad) == 0 &&
                Objects.equals(gapToLeader, that.gapToLeader) &&
                Objects.equals(tyreCompound, that.tyreCompound) &&
                Objects.equals(weather, that.weather) &&
                Objects.equals(lastLapTime, that.lastLapTime) &&
                Objects.equals(driverMessage, that.driverMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lap, totalLaps, position, gapToLeader, tyreCompound, tyreAge, fuelLoad, weather, lastLapTime, driverMessage);
    }

    @Override
    public String toString() {
        return "RaceContextRequest{" +
                "lap=" + lap +
                ", totalLaps=" + totalLaps +
                ", position=" + position +
                ", gapToLeader='" + gapToLeader + '\'' +
                ", tyreCompound='" + tyreCompound + '\'' +
                ", tyreAge=" + tyreAge +
                ", fuelLoad=" + fuelLoad +
                ", weather='" + weather + '\'' +
                ", lastLapTime='" + lastLapTime + '\'' +
                ", driverMessage='" + driverMessage + '\'' +
                '}';
    }
}
