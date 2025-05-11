package com.clientServer.participantRepo;

import com.clientServer.JdbcUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.clientServer.entities.Participant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ParticipantRepository implements IParticipantRepository {
    private JdbcUtils dbUtils;
    private static final Logger logger = LogManager.getLogger();

    public ParticipantRepository(Properties properties) {
        logger.info("Initializing Participant Repository with props:{}", properties);
        dbUtils = new JdbcUtils(properties);
    }

    private Participant extractEntity(ResultSet resultSet) throws SQLException {
        Integer id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        Integer age = resultSet.getInt("age");
        Participant participant = new Participant(name, age);
        participant.setId(id);
        return participant;
    }

    @Override
    public Participant findOne(Integer integer) {
        logger.traceEntry("Finding Participant with id: {}", integer);
        Connection connection = dbUtils.getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM participants WHERE id = ?")) {
            preparedStatement.setInt(1, integer);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Participant participant = extractEntity(resultSet);
                    logger.traceExit("Found Participant with id: {}", integer);
                    return participant;
                }
            }

        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit("No participant found with id: {}", integer);
        return null;
    }

    @Override
    public Iterable<Participant> findAll() {
        Connection connection = dbUtils.getConnection();
        List<Participant> participants = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("select * from participants")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Participant participant = extractEntity(resultSet);
                    participants.add(participant);
                }
            } catch (SQLException e) {
                logger.error(e);
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit("Found {} participants", participants);
        return participants;
    }

    @Override
    public void save(Participant entity) {
        logger.traceEntry("Saving Participant: {}", entity);
        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO participants(name, age) VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, entity.getName());
            preparedStatement.setInt(2, entity.getAge());
            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating participant failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit("Saving Participant: {}", entity);
    }

    @Override
    public void delete(Integer integer) {
        logger.traceEntry("Deleting Participant with id: {}", integer);
        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM participants WHERE id=?")) {
            preparedStatement.setInt(1, integer);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit("Deleting Participant with id: {}", integer);

    }

    @Override
    public void update(Participant entity) {
        logger.traceEntry("Updating Participant: {}", entity);
        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE participants SET name=?, age=? WHERE id=?")) {
            preparedStatement.setString(1, entity.getName());
            preparedStatement.setInt(2, entity.getAge());
            preparedStatement.setInt(3, entity.getId());
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit(entity);
    }

    @Override
    public Participant findParticipantByNameAndAge(String name, Integer age) {
        Participant participant = null;
        logger.traceEntry("Finding participant with name: " + name + " and age: " + age);
        Connection connection = dbUtils.getConnection();
        try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM participants WHERE name = ? and age = ?")){
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, age);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    participant = extractEntity(resultSet);
                    logger.traceExit("Found Participant with name: " + name + " and age: " + age);
                }

            }
        }catch (SQLException e) {
            logger.error(e);
        }
        return participant;
    }
}


