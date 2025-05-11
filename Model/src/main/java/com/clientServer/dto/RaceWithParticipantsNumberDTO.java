package com.clientServer.dto;

import com.clientServer.entities.SwimmingRace;

import java.io.Serializable;

public class RaceWithParticipantsNumberDTO implements Serializable{
    private SwimmingRace swimmingRace;
    private Integer participantsNumber;

    public RaceWithParticipantsNumberDTO(SwimmingRace swimmingRace, Integer participantsNumber) {
        this.swimmingRace = swimmingRace;
        this.participantsNumber = participantsNumber;
    }

    public SwimmingRace getSwimmingRace() {
        return swimmingRace;
    }

    public void setSwimmingRace(SwimmingRace swimmingRace) {
        this.swimmingRace = swimmingRace;
    }

    public Integer getParticipantsNumber() {
        return participantsNumber;
    }

    public void setParticipantsNumber(Integer participantsNumber) {
        this.participantsNumber = participantsNumber;
    }

    @Override
    public String toString() {
        return "RaceWithParticipantsNumberDTO{" +
                "swimmingRace=" + swimmingRace +
                ", participantsNumber=" + participantsNumber +
                '}';
    }
}
