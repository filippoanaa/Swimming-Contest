package com.clientServer.entities;

public class Participation extends Entity<Tuple<Integer, Integer>> {
    private Participant participant;
    private SwimmingRace swimmingRace;

    public Participation() {}
    public Participation(Participant participant, SwimmingRace swimmingRace) {
        this.participant = participant;
        this.swimmingRace = swimmingRace;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public SwimmingRace getSwimmingRace() {
        return swimmingRace;
    }

    public void setSwimmingRace(SwimmingRace swimmingRace) {
        this.swimmingRace = swimmingRace;
    }

    @Override
    public String toString() {
        return "Participation{" +
                "participant=" + participant.getName() +
                ", swimmingRace=" + swimmingRace.getStyle() +
                '}';
    }
}
