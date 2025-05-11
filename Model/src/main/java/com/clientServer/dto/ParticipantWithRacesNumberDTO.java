package com.clientServer.dto;


import com.clientServer.entities.Participant;

import java.io.Serializable;

public class ParticipantWithRacesNumberDTO implements Serializable {
    private Participant participant;
    private Integer racesNumber;

    public ParticipantWithRacesNumberDTO(Participant participant, Integer racesNumber) {
        this.participant = participant;
        this.racesNumber = racesNumber;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }
    public Integer getRacesNumber() {
        return racesNumber;
    }
    public void setRacesNumber(Integer racesNumber) {
        this.racesNumber = racesNumber;
    }
}
