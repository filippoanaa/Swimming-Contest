package com.clientServer.dto;

import com.clientServer.entities.Participant;
import com.clientServer.entities.SwimmingRace;

import java.io.Serializable;

public class ParticipationDTO implements Serializable {
    private Participant participant;
    private Iterable<SwimmingRace> swimmingRaces;

    public ParticipationDTO(Participant participant, Iterable<SwimmingRace> swimmingRaces) {
        this.participant = participant;
        this.swimmingRaces = swimmingRaces;
    }

    public Participant getParticipant() {
        return participant;
    }
    public Iterable<SwimmingRace> getSwimmingRaces() {
        return swimmingRaces;
    }


}
