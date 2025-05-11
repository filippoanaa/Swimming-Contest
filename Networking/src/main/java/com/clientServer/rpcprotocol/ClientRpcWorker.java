package com.clientServer.rpcprotocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.clientServer.dto.ParticipantWithRacesNumberDTO;
import com.clientServer.dto.ParticipationDTO;
import com.clientServer.dto.RaceWithParticipantsNumberDTO;
import com.clientServer.entities.SwimmingRace;
import com.clientServer.entities.User;
import com.clientServer.IObserver;
import com.clientServer.IServices;
import com.clientServer.SwimmingContestException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;


public class ClientRpcWorker implements Runnable, IObserver {
    private IServices server;
    private Socket connection;

    private ObjectInputStream input;
    private ObjectOutputStream output;
    private volatile boolean connected;

    private static Logger logger = LogManager.getLogger(ClientRpcWorker.class);

    public ClientRpcWorker(IServices server, Socket connection) {
        this.server = server;
        this.connection = connection;
        try {
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            connected = true;
        } catch (IOException e) {
            logger.error(e);
            logger.error(e.getStackTrace());
            connected = false;
        }
    }

    @Override
    public void run() {
        while (connected) {
            try {
                Object request = input.readObject();
                logger.debug("Received request: {}", request);
                System.out.println("Received request: " + request);
                Response response = handleRequest((Request) request);

                if (response != null) {
                    logger.debug("Response sent: {}", response);
                    System.out.println("Response sent: " + response);
                    sendResponse(response);
                }

            } catch (IOException | ClassNotFoundException e) {
                logger.error(e);
                logger.error(e.getStackTrace());
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error(e);
                logger.error(e.getStackTrace());
            }
        }

        try {
            input.close();
            output.close();
            connection.close();
        } catch (IOException e) {
            logger.error(e);
        }
    }


    @Override
    public void updateRaces(Iterable<RaceWithParticipantsNumberDTO> races) {
        Response response = new Response.Builder().type(ResponseType.NEW_PARTICIPATION).data(races).build();
        try {
            logger.debug("i m in worker trying to update");
            sendResponse(response);
        } catch (IOException e) {
            logger.error(e);
        }
    }


    private static final Response okResponse = new Response.Builder().type(ResponseType.OK).build();

    private Response handleRequest(Request request) {
        Response response = null;
        String handlerName = "handle" + (request).type();
        logger.debug("HandlerName {}", handlerName); //dinamic
        try {
            Method method = this.getClass().getDeclaredMethod(handlerName, Request.class); //reflecsion
            response = (Response) method.invoke(this, request);
            logger.debug("Method {} ", handlerName + " invoked");
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            logger.error(e);
            logger.error(e.getStackTrace());
        }
        return response;
    }

    private void sendResponse(Response response) throws IOException {
        logger.debug("Sending response {}", response);
        synchronized (output) {
            output.writeObject(response);
            output.flush();
        }
    }


    private Response handleLOGIN(Request request) {
        logger.debug("Login request{}", request);
        User user = (User) request.data();

        try {
            server.login(user, this);
            return okResponse;
        } catch (SwimmingContestException e) {
            connected = false;
            return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
        }
    }

    private Response handleLOGOUT(Request request) {
        logger.debug("Logout request{}", request);
        User user = (User) request.data();

        try {
            server.logout(user, this);
            connected = false;
            return okResponse;
        } catch (SwimmingContestException e) {
            return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
        }
    }


    private Response handleGET_RACES(Request request) {
        logger.debug("Get races request{}", request);
        try {
            Iterable<RaceWithParticipantsNumberDTO> racesWithParticipantsCount = server.getRacesWithParticipants();
            return new Response.Builder().type(ResponseType.RACES_RESULT).data(racesWithParticipantsCount).build();
        } catch (SwimmingContestException e) {
            return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
        }
    }

    private Response handleCREATE_PARTICIPATION(Request request) {
        logger.debug("Add participant request received: {}", request);
        ParticipationDTO participationDTO = (ParticipationDTO) request.data();

        try {
            server.createParticipation(participationDTO);
            logger.debug("Participant added successfully: {}", participationDTO);

            Iterable<RaceWithParticipantsNumberDTO> updatedRaces = server.getRacesWithParticipants();
            logger.debug("Updated races after participation added: {}", updatedRaces);

            return okResponse;  //update from observer

        } catch (SwimmingContestException e) {
            logger.error("Error adding participant: {}", e.getMessage());
            return new Response.Builder()
                    .type(ResponseType.ERROR)
                    .data(e.getMessage())
                    .build();
        }
    }

    private Response handleSEARCH_PARTICIPANTS(Request request) {
        logger.debug("Search participants request{}", request);

        SwimmingRace swimmingRace = (SwimmingRace) request.data();
        SwimmingRace.DistanceType distanceType = swimmingRace.getDistanceType();
        SwimmingRace.Style style = swimmingRace.getStyle();

        try {
            Iterable<ParticipantWithRacesNumberDTO> participantsWithRaces = server.getParticipantsWithRacesCountFromRace(distanceType, style);
            return new Response.Builder().type(ResponseType.SEARCH_RESULT).data(participantsWithRaces).build();
        } catch (SwimmingContestException e) {
            return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
        }
    }


}
