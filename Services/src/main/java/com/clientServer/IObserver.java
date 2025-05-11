package com.clientServer;

import com.clientServer.dto.RaceWithParticipantsNumberDTO;

public interface IObserver {
    void updateRaces(Iterable<RaceWithParticipantsNumberDTO> races) throws SwimmingContestException;

}
