package com.clientServer.participationRepo;

import com.clientServer.IRepository;
import com.clientServer.entities.Participant;
import com.clientServer.entities.Participation;
import com.clientServer.entities.Tuple;

public interface IParticipationRepository extends IRepository<Tuple<Integer, Integer>, Participation> {
    boolean existsParticipation(Tuple<Integer, Integer> id);
    Iterable<Participant> findParticipantsFromASwimmingRace(Integer swimmingRaceId);
    Integer findNumberOfParticipantsForSwimmingRace(Integer swimmingRaceId);
    Integer findNumberOfSwimmingRacesForParticipant(Integer participationId);
}
