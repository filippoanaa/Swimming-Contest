package com.clientServer.userRepo;

import com.clientServer.JdbcUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.clientServer.entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class UserRepository implements IUserRepository {
    private JdbcUtils dbUtils;
    private static final Logger logger = LogManager.getLogger();

    public UserRepository(Properties properties) {
        logger.info("Initializing User Repository with props:{}", properties);
        dbUtils = new JdbcUtils(properties);
    }

    private User extractEntity(ResultSet resultSet) throws SQLException {
        Integer id = resultSet.getInt("id");
        String username = resultSet.getString("username");
        String password = resultSet.getString("password");
        User user = new User(username, password);
        user.setId(id);
        return user;
    }

    @Override
    public User findOne(Integer integer) {
        logger.traceEntry("Finding User with id: {}", integer);
        Connection connection = dbUtils.getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            preparedStatement.setInt(1, integer);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    User user = extractEntity(resultSet);
                    logger.traceExit("Found User with id: {}", integer);
                    return user;
                }
            }

        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit("No user found with id: {}", integer);
        return null;
    }

    @Override
    public Iterable<User> findAll() {
        Connection connection = dbUtils.getConnection();
        List<User> users = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    User user = extractEntity(resultSet);
                    users.add(user);
                }
            } catch (SQLException e) {
                logger.error(e);
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit("Found {} users", users);
        return users;
    }

    @Override
    public void save(User entity) {
        logger.traceEntry("Saving User: {}", entity);
        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users(username, password) VALUES (?, ?)")) {
            preparedStatement.setString(1, entity.getUsername());
            preparedStatement.setString(2, entity.getPassword());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit("Saving User: {}", entity);
    }

    @Override
    public void delete(Integer integer) {
        logger.traceEntry("Deleting User with id: {}", integer);
        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM users WHERE id=?")) {
            preparedStatement.setInt(1, integer);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit("Deleting User with id: {}", integer);
    }

    @Override
    public void update(User entity) {
        logger.traceEntry("Updating User: {}", entity);
        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET username=?, password=? WHERE id=?")) {
            preparedStatement.setString(1, entity.getUsername());
            preparedStatement.setString(2, entity.getPassword());
            preparedStatement.setInt(3, entity.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.traceExit(entity);
    }

    @Override
    public User findUserByUsername(String username) {
        logger.traceEntry("Finding user with username: " + username);
        User user = null;
        Connection connection = dbUtils.getConnection();
        try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ?")){
            preparedStatement.setString(1, username);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    user = extractEntity(resultSet);
                    logger.traceExit("Found User with name: {}", username);
                }
            }
        } catch (SQLException e) {
           logger.error(e);
        }
        logger.traceExit("Found user with name: {}", username);
        return user;
    }
}