package com.clientServer.participantRepo;

import com.clientServer.IRepository;
import com.clientServer.entities.Participant;

public interface IParticipantRepository extends IRepository<Integer, Participant> {
    Participant findParticipantByNameAndAge(String name, Integer age);
}
