package com.clientServer.participationRepo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.clientServer.entities.Participant;
import com.clientServer.entities.Participation;
import com.clientServer.entities.SwimmingRace;
import com.clientServer.entities.Tuple;
import ro.mpp2024.repository.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ParticipationRepository implements IParticipationRepository {
    private JdbcUtils dbUtils;
    private static final Logger logger = LogManager.getLogger();

    public ParticipationRepository(Properties properties) {
        logger.info("Initializing Participation Repository with props:{}", properties);
        dbUtils = new JdbcUtils(properties);
    }

    private Participation extractEntity(ResultSet resultSet) throws SQLException {
        Integer participantId = resultSet.getInt("participant_id");
        String name = resultSet.getString("name");
        Integer age  = resultSet.getInt("age");
        Participant participant = new Participant(name, age);
        participant.setId(participantId);


        Integer swimmingRaceId = resultSet.getInt("swimming_race_id");
        SwimmingRace.DistanceType distanceType = SwimmingRace.DistanceType.valueOf(resultSet.getString("distance"));
        SwimmingRace.Style style = SwimmingRace.Style.valueOf(resultSet.getString("style"));
        SwimmingRace swimmingRace = new SwimmingRace(distanceType, style);
        swimmingRace.setId(swimmingRaceId);


        Participation participation = new Participation(participant, swimmingRace);
        participation.setId(new Tuple<>(participantId, swimmingRaceId));
        return participation;
    }

    @Override
    public Participation findOne(Tuple<Integer, Integer> tuple) {
        logger.traceEntry("Finding Participation with id: {}", tuple);
        Connection connection = dbUtils.getConnection();
        Participation participation = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT p.participant_id, p.swimming_race_id, participant.name, participant.age, sr.distance, sr.style FROM participations p " +
                "INNER JOIN participants participant ON p.participant_id = participant.id " +
                "INNER JOIN swimming_races sr ON p.swimming_race_id = sr.id " +
                "WHERE p.participant_id = ? AND p.swimming_race_id = ?")) {
            preparedStatement.setInt(1, tuple.getE1());
            preparedStatement.setInt(2, tuple.getE2());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    participation = extractEntity(resultSet);
                    logger.traceExit("Found Participation with id: {}", tuple);
                }
            }

        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit("No participation found with id: {}", tuple);
        return participation;
    }

    @Override
    public Iterable<Participation> findAll() {
        Connection connection = dbUtils.getConnection();
        List<Participation> participations = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT p.participant_id, p.swimming_race_id, participant.name, participant.age, sr.distance, sr.style FROM participations p " +
                        "INNER JOIN participants participant ON p.participant_id = participant.id " +
                        "INNER JOIN swimming_races sr ON p.swimming_race_id = sr.id")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Participation participation = extractEntity(resultSet);
                    participations.add(participation);
                }
            } catch (SQLException e) {
                logger.error(e);
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit("Found {} participations", participations.size());
        return participations;
    }

    @Override
    public void save(Participation entity) {
        logger.traceEntry("Saving Participation: {}", entity);
        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO participations(participant_id, swimming_race_id) VALUES (?, ?)")) {
            preparedStatement.setInt(1, entity.getParticipant().getId());
            preparedStatement.setInt(2, entity.getSwimmingRace().getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit("Saving Participation: {}", entity);
    }

    @Override
    public void delete(Tuple<Integer, Integer> tuple) {
        logger.traceEntry("Deleting Participation with id: {}", tuple);
        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "DELETE FROM participations WHERE participant_id=? AND swimming_race_id=?")) {
            preparedStatement.setInt(1, tuple.getE1());
            preparedStatement.setInt(2, tuple.getE2());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit("Deleting Participation with id: {}", tuple);
    }

    @Override
    public void update(Participation entity) {

    }

    @Override
    public boolean existsParticipation(Tuple<Integer, Integer> id) {
        Participation participation = findOne(id);
        return participation != null;
    }


    @Override
    public Iterable<Participant> findParticipantsFromASwimmingRace(Integer swimmingRaceId){
        logger.traceEntry("Finding participants from the swimming race: {}", swimmingRaceId);
        List<Participant> participants = new ArrayList<>();
        Connection connection = dbUtils.getConnection();
        try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT participant.id, participant.name, participant.age " +
                "FROM participants participant " +
                "INNER JOIN participations p ON participant.id = p.participant_id " +
                "WHERE p.swimming_race_id = ?"
        )){
            preparedStatement.setInt(1, swimmingRaceId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                Integer id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                Integer age = resultSet.getInt("age");
                Participant participant = new Participant(name, age);
                participant.setId(id);
                participants.add(participant);
            }

        }catch (SQLException e){
            logger.error(e);
        }
        return participants;
    }


    @Override
    public Integer findNumberOfParticipantsForSwimmingRace(Integer swimmingRaceId) {
        logger.traceEntry("Calculating number of participants for swimming race: {}", swimmingRaceId);
        Integer participantsCount = 0;
        Connection connection = dbUtils.getConnection();
        String sql = "SELECT COUNT(p.participant_id) AS participants_count FROM participations p " +
                "WHERE p.swimming_race_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, swimmingRaceId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                participantsCount = resultSet.getInt("participants_count");
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return participantsCount;
    }

    @Override
    public Integer findNumberOfSwimmingRacesForParticipant(Integer participationId) {
        logger.traceEntry("Calculating number of swimming races for for participant: {}", participationId);
        Integer races_count = 0;
        Connection connection = dbUtils.getConnection();
        String sql = "SELECT COUNT(p.swimming_race_id) AS races_count FROM participations p " +
                "WHERE p.participant_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, participationId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                races_count = resultSet.getInt("races_count");
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return races_count;
    }
}
