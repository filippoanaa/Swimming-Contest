package com.clientServer.entities;


import java.util.Objects;

public class SwimmingRace extends Entity<Integer> {
    private DistanceType distanceType;
    private Style style;

    public enum DistanceType{
        _50m, _200m, _800m, _1500m
    }
    public enum Style{
        free, back, butterflyStock, mixt
    }

    public SwimmingRace() {}
    public SwimmingRace(DistanceType distanceType, Style style) {
        this.distanceType = distanceType;
        this.style = style;
    }

    public DistanceType getDistanceType() {
        return distanceType;
    }

    public void setDistanceType(DistanceType distanceType) {
        this.distanceType = distanceType;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) { this.style = style; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SwimmingRace that = (SwimmingRace) o;
        return distanceType == that.distanceType && style == that.style ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(distanceType, style);
    }

    @Override
    public String toString() {
        return "SwimmingRace{" +
                "distanceType=" + distanceType +
                ", style=" + style +
                '}';
    }


}

