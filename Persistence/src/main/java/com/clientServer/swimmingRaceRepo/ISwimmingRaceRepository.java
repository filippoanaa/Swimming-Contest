package com.clientServer.swimmingRaceRepo;

import com.clientServer.IRepository;
import com.clientServer.entities.SwimmingRace;

public interface ISwimmingRaceRepository extends IRepository<Integer, SwimmingRace> {
    SwimmingRace findByStyleAndDistance(SwimmingRace.Style style, SwimmingRace.DistanceType distance);
}
