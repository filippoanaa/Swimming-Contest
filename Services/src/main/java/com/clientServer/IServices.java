package com.clientServer;

import com.clientServer.dto.ParticipantWithRacesNumberDTO;
import com.clientServer.dto.ParticipationDTO;
import com.clientServer.dto.RaceWithParticipantsNumberDTO;
import com.clientServer.entities.SwimmingRace;
import com.clientServer.entities.User;

public interface IServices {
    void login(User user, IObserver observer) throws SwimmingContestException;
    void logout(User user, IObserver observer) throws SwimmingContestException;
    void createParticipation(ParticipationDTO participation) throws SwimmingContestException;
    Iterable<RaceWithParticipantsNumberDTO> getRacesWithParticipants() throws SwimmingContestException;
    Iterable<ParticipantWithRacesNumberDTO> getParticipantsWithRacesCountFromRace(SwimmingRace.DistanceType distance, SwimmingRace.Style style) throws SwimmingContestException;
}
