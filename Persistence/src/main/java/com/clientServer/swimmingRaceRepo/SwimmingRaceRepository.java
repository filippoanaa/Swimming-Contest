package com.clientServer.swimmingRaceRepo;

import com.clientServer.JdbcUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.clientServer.entities.SwimmingRace;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SwimmingRaceRepository implements ISwimmingRaceRepository{
    private JdbcUtils dbUtils;
    private static final Logger logger = LogManager.getLogger();

    public SwimmingRaceRepository(Properties properties) {
        logger.info("Initializing SwimmingRace Repository with props:{}", properties);
        dbUtils = new JdbcUtils(properties);
    }

    private SwimmingRace extractEntity(ResultSet resultSet) throws SQLException {
        Integer id = resultSet.getInt("id");
        SwimmingRace.DistanceType distanceType = SwimmingRace.DistanceType.valueOf(resultSet.getString("distance"));
        SwimmingRace.Style style = SwimmingRace.Style.valueOf(resultSet.getString("style"));
        SwimmingRace swimmingRace = new SwimmingRace(distanceType, style);
        swimmingRace.setId(id);
        return swimmingRace;
    }

    @Override
    public SwimmingRace findOne(Integer integer) {
        logger.traceEntry("Finding SwimmingRace with id: {}", integer);
        Connection connection = dbUtils.getConnection();
        SwimmingRace swimmingRace = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM swimming_races WHERE id = ?")) {
            preparedStatement.setInt(1, integer);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    swimmingRace = extractEntity(resultSet);
                    logger.traceExit("Found SwimmingRace with id: {}", integer);
                    return swimmingRace;
                }
            }

        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit("No swimming race found with id: {}", integer);
        return swimmingRace;
    }

    @Override
    public Iterable<SwimmingRace> findAll() {
        Connection connection = dbUtils.getConnection();
        List<SwimmingRace> swimmingRaces = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM swimming_races")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    SwimmingRace swimmingRace = extractEntity(resultSet);
                    swimmingRaces.add(swimmingRace);
                }
            } catch (SQLException e) {
                logger.error(e);
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit("Found {} swimming races", swimmingRaces.size());
        return swimmingRaces;
    }

    @Override
    public void save(SwimmingRace entity) {
        logger.traceEntry("Saving SwimmingRace: {}", entity);
        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO swimming_races(distance, style) VALUES (?, ?)")) {
            preparedStatement.setString(1, entity.getDistanceType().name());
            preparedStatement.setString(2, entity.getStyle().name());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit("Saving SwimmingRace: {}", entity);
    }

    @Override
    public void delete(Integer integer) {
        logger.traceEntry("Deleting SwimmingRace with id: {}", integer);
        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM swimming_races WHERE id=?")) {
            preparedStatement.setInt(1, integer);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit("Deleting SwimmingRace with id: {}", integer);
    }

    @Override
    public void update(SwimmingRace entity) {
        logger.traceEntry("Updating SwimmingRace: {}", entity);
        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE swimming_races SET distance=?, style=? WHERE id=?")) {
            preparedStatement.setString(1, entity.getDistanceType().name());
            preparedStatement.setString(2, entity.getStyle().name());
            preparedStatement.setInt(3, entity.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit(entity);
    }

    @Override
    public SwimmingRace findByStyleAndDistance(SwimmingRace.Style style, SwimmingRace.DistanceType distance) {
        logger.traceEntry("Finding SwimmingRace with id: {,}", style, distance);
        Connection connection = dbUtils.getConnection();
        SwimmingRace swimmingRace = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM swimming_races WHERE style=? AND distance=?")) {
            preparedStatement.setString(1, style.name());
            preparedStatement.setString(2, distance.name());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    swimmingRace = extractEntity(resultSet);
                    logger.traceExit("Found SwimmingRace by style and distance");
                    return swimmingRace;
                }
            }

        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit("No swimming race found with style and distance");
        return swimmingRace;
    }
}
