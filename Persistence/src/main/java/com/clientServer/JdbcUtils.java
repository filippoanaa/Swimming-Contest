package com.clientServer;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcUtils {
    private Properties properties;
    private static final Logger logger = LogManager.getLogger();
    public JdbcUtils(Properties properties) {
        this.properties = properties;
    }

    private Connection instance = null;
    private Connection getNewConnection() throws SQLException {
        logger.traceEntry();

        String url = properties.getProperty("url");
        String user = properties.getProperty("user");
        String password = properties.getProperty("password");
        logger.info("Connecting to database...{}", url);
        logger.info("user: {}", user);
        logger.info("password: {}", password);

        Connection connection = null;
        try{
            if(user != null && password != null) {
                connection = DriverManager.getConnection(url, user, password);
            }else{
                connection = DriverManager.getConnection(url);
            }
        }catch (SQLException e) {
            logger.error(e);
            System.out.println("Error connecting to database" +  e.getMessage());

        }
        return connection;
    }

    public Connection getConnection(){
        logger.traceEntry();
        try{
            if(instance == null || !instance.isClosed()) {
                instance = getNewConnection();
            }
        }catch (SQLException e) {
            logger.error(e);
            System.out.println("Error connecting to database" +  e.getMessage());
        }
        logger.traceExit(instance);
        return instance;
    }


}
