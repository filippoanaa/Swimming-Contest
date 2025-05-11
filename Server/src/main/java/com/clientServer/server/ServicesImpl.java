package com.clientServer.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.clientServer.dto.ParticipantWithRacesNumberDTO;
import com.clientServer.dto.ParticipationDTO;
import com.clientServer.dto.RaceWithParticipantsNumberDTO;
import com.clientServer.entities.*;
import com.clientServer.participantRepo.IParticipantRepository;
import com.clientServer.participationRepo.IParticipationRepository;
import com.clientServer.swimmingRaceRepo.ISwimmingRaceRepository;
import com.clientServer.userRepo.IUserRepository;
import com.clientServer.server.utils.PasswordUtils;
import com.clientServer.IObserver;
import com.clientServer.IServices;
import com.clientServer.SwimmingContestException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.StreamSupport;

public class ServicesImpl implements IServices {

    private IUserRepository userRepo;
    private IParticipantRepository participantRepo;
    private IParticipationRepository participationRepo;
    private ISwimmingRaceRepository swimmingRaceRepo;
    private ConcurrentHashMap<String, IObserver> loggedUsers;

    private static Logger logger = LogManager.getLogger(ServicesImpl.class);

    private final int defaultThreadNo = 3;


    public ServicesImpl(IUserRepository userRepo, IParticipationRepository participationRepo, IParticipantRepository participantRepo, ISwimmingRaceRepository raceRepo) {
        this.userRepo = userRepo;
        this.participationRepo = participationRepo;
        this.participantRepo = participantRepo;
        this.swimmingRaceRepo = raceRepo;
        this.loggedUsers = new ConcurrentHashMap<>();
        encryptExistingPasswords();
    }

    @Override
    public synchronized void login(User user, IObserver chatObserver) throws SwimmingContestException {
        if (verifyCredentials(user.getUsername(), user.getPassword())) {
            if (loggedUsers.containsKey(user.getUsername())) {
                throw new SwimmingContestException("User already logged in");
            }

            loggedUsers.put(user.getUsername(), chatObserver);
        } else {
            throw new SwimmingContestException("Authentication failed.");
        }
    }

    private boolean verifyCredentials(String username, String password) {
        User user = userRepo.findUserByUsername(username);
        if (user == null) {
            logger.error("User not found for username: {}", username);
            return false;
        }
        boolean passwordMatches = PasswordUtils.checkPassword(password, user.getPassword());
        if (!passwordMatches) {
            logger.error("Password does not match for username: {}", username);
        }
        return passwordMatches;
    }

    @Override
    public synchronized void logout(User user, IObserver observer) throws SwimmingContestException {
        IObserver client = loggedUsers.get(user.getUsername());
        if (client == null) {
            throw new SwimmingContestException("User: " + user.getUsername() + " not logged in");
        }
        loggedUsers.remove(user.getUsername());
    }


    @Override
    public Iterable<RaceWithParticipantsNumberDTO> getRacesWithParticipants() throws SwimmingContestException {
        Iterable<SwimmingRace> races = swimmingRaceRepo.findAll();
        List<RaceWithParticipantsNumberDTO> result = new ArrayList<>();

        for (SwimmingRace race : races) {
            int participantsCount = participationRepo.findNumberOfParticipantsForSwimmingRace(race.getId());
            result.add(new RaceWithParticipantsNumberDTO(race, participantsCount));
        }
        return result;

    }

    @Override
    public Iterable<ParticipantWithRacesNumberDTO> getParticipantsWithRacesCountFromRace(SwimmingRace.DistanceType distance, SwimmingRace.Style style) throws SwimmingContestException {
        SwimmingRace swimmingRace = swimmingRaceRepo.findByStyleAndDistance(style, distance);
        List<ParticipantWithRacesNumberDTO> result = new ArrayList<>();

        if (swimmingRace == null) {
            return result;
        }
        Iterable<Participant> participants = participationRepo.findParticipantsFromASwimmingRace(swimmingRace.getId());
        for (Participant p : participants) {
            int racesCount = participationRepo.findNumberOfSwimmingRacesForParticipant(p.getId());
            result.add(new ParticipantWithRacesNumberDTO(p, racesCount));
        }

        return result;
    }

    @Override
    public synchronized void createParticipation(ParticipationDTO participationDTO) throws SwimmingContestException {
        Participant participant = participationDTO.getParticipant();
        Iterable<SwimmingRace> races = participationDTO.getSwimmingRaces();

        Participant foundParticipant = participantRepo.findParticipantByNameAndAge(participant.getName(), participant.getAge());

        if (foundParticipant == null) {
            participantRepo.save(participant);
            foundParticipant = participantRepo.findParticipantByNameAndAge(participant.getName(), participant.getAge());
        }

        for (SwimmingRace swimmingRace : races) {
            Tuple<Integer, Integer> participationId = new Tuple<>(foundParticipant.getId(), swimmingRace.getId());
            if (!participationRepo.existsParticipation(participationId)) {
                Participation participation = new Participation(foundParticipant, swimmingRace);
                participation.setId(participationId);
                participationRepo.save(participation);
            }
        }

        List<RaceWithParticipantsNumberDTO> all = new ArrayList<>();

        Iterable<SwimmingRace> sr = swimmingRaceRepo.findAll();
        for (SwimmingRace s : sr) {
            int participantsCount = participationRepo.findNumberOfParticipantsForSwimmingRace(s.getId());
            all.add(new RaceWithParticipantsNumberDTO(s, participantsCount));
        }
        notifyUsers(all);

    }


    private void notifyUsers(Iterable<RaceWithParticipantsNumberDTO> races) {
        logger.info("!!!!!!!!!Notifying users logged in");
        ExecutorService executor = Executors.newFixedThreadPool(defaultThreadNo);
        for (String user : loggedUsers.keySet()) {
            IObserver client = loggedUsers.get(user);
            if (client != null) {
                executor.execute(() -> {
                    try {
                        logger.info("Notifying user {}", user);
                        client.updateRaces(races);
                    } catch (Exception e) {
                        logger.error("Error notifying user {}", user, e);
                    }
                });
            }
        }
        executor.shutdown();
    }

    private void encryptExistingPasswords() {
        List<User> users = StreamSupport.stream(userRepo.findAll().spliterator(), false).toList();
        for (User user : users) {
            if (!PasswordUtils.isHashed(user.getPassword())) {
                String password = user.getPassword();
                String hashedPassword = PasswordUtils.hashPassword(password);
                user.setPassword(hashedPassword);
                userRepo.update(user);
            }
        }

    }

    public void addUser(User user) throws SwimmingContestException {
        if (userRepo.findUserByUsername(user.getUsername()) != null) {
            throw new SwimmingContestException("User with username already exists");
        }

        String hashedPassword = PasswordUtils.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);
        userRepo.save(user);

    }


}
