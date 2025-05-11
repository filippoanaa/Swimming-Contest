package com.clientServer;

import com.clientServer.participantRepo.ParticipantRepository;
import com.clientServer.server.ServicesImpl;
import com.clientServer.swimmingRaceRepo.SwimmingRaceRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.clientServer.utils.AbstractServer;
import com.clientServer.utils.RpcConcurrentServer;
import com.clientServer.utils.ServerException;
import com.clientServer.participantRepo.IParticipantRepository;
import com.clientServer.participationRepo.IParticipationRepository;
import com.clientServer.participationRepo.ParticipationRepository;
import com.clientServer.swimmingRaceRepo.ISwimmingRaceRepository;
import com.clientServer.userRepo.IUserRepository;
import com.clientServer.userRepo.UserRepository;
import com.clientServer.IServices;

import java.io.File;
import java.util.Properties;

public class StartRpcServer {
    private static int defaultPort=55555;
    private static Logger logger = LogManager.getLogger(StartRpcServer.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        try{
            properties.load(StartRpcServer.class.getResourceAsStream("/server.properties"));
            logger.info("Server properties set: {}", properties);
        }catch (Exception e){
            logger.error("Cannot find server.properties {}", String.valueOf(e));
            logger.debug("Looking for file in {}", (new File(".")).getAbsolutePath());
            return;
        }

        IUserRepository userRepository = new UserRepository(properties);
        IParticipationRepository participationRepository= new ParticipationRepository(properties);
        IParticipantRepository participantRepository = new ParticipantRepository(properties);
        ISwimmingRaceRepository swimmingRaceRepository = new SwimmingRaceRepository(properties);

        IServices serverImpl = new ServicesImpl(userRepository, participationRepository,participantRepository, swimmingRaceRepository);

        int serverPort = defaultPort;
        try{
            serverPort = Integer.parseInt(properties.getProperty("server.port"));
        }catch (NumberFormatException e){
            logger.error("Wrong  Port Number{}", e.getMessage());
            logger.debug("Using default port {}", defaultPort);
        }


        logger.debug("Starting server on port: {}", serverPort);
        AbstractServer server = new RpcConcurrentServer(serverPort, serverImpl);

        try{
            server.start();
        } catch (ServerException e) {
            logger.error("Error starting the server{}", e.getMessage());
        }finally {
            try{
                server.stop();
            }catch (ServerException e){
                logger.error("Error stopping the server{}", e.getMessage());
            }
        }


    }
}