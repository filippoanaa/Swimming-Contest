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
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ServicesRpcProxy implements IServices {
    private String host;
    private int port;

    private static Logger logger = LogManager.getLogger(ServicesRpcProxy.class);

    private IObserver client;

    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Socket connection;

    private BlockingQueue<Response> queueResponses;
    private volatile boolean finished;

    public ServicesRpcProxy(String host, int port) {
        this.host = host;
        this.port = port;
        queueResponses = new LinkedBlockingQueue<>();
    }

    private void initializeConnection() throws SwimmingContestException {
        try {
            connection = new Socket(host, port);
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            finished = false;
            startReader();

        } catch (IOException e) {
            logger.error("Error initializing connection {}", String.valueOf(e));
            logger.error(e.getStackTrace());
        }
    }


    @Override
    public void login(User user, IObserver newClient) throws SwimmingContestException {
        initializeConnection();
        Request request = new Request.Builder().type(RequestType.LOGIN).data(user).build();
        sendRequest(request);

        Response response = readResponse();
        if (response.type() == ResponseType.OK) {
            this.client = newClient;
            return;
        }

        if (response.type() == ResponseType.ERROR) {
            String error = response.data().toString();
            closeConnection();
            throw new SwimmingContestException(error);
        }
    }

    private void closeConnection() {
        logger.info("Closing connection");
        finished = true;
        try {
            input.close();
            output.close();
            connection.close();
            client = null;
        } catch (IOException e) {
            logger.error(e);
            logger.error(e.getStackTrace());
        }
    }

    @Override
    public void logout(User user, IObserver observer) throws SwimmingContestException {
        Request request = new Request.Builder().type(RequestType.LOGOUT).data(user).build();
        sendRequest(request);

        Response response = readResponse();
        closeConnection();

        if (response.type() == ResponseType.ERROR) {
            throw new SwimmingContestException(response.data().toString());
        }

    }

    @Override
    public void createParticipation(ParticipationDTO participationDTO) throws SwimmingContestException {
        Request request = new Request.Builder().type(RequestType.CREATE_PARTICIPATION).data(participationDTO).build();
        sendRequest(request);

        Response response = readResponse();
        if (response.type() == ResponseType.ERROR) {
            throw new SwimmingContestException(response.data().toString());
        }
    }

    @Override
    public Iterable<RaceWithParticipantsNumberDTO> getRacesWithParticipants() throws SwimmingContestException {
        Request request = new Request.Builder().type(RequestType.GET_RACES).build();
        sendRequest(request);

        Response response = readResponse();

        if (response.type() == ResponseType.ERROR) {
            throw new SwimmingContestException(response.data().toString());
        }

        return (Iterable<RaceWithParticipantsNumberDTO>) response.data();
    }

    @Override
    public Iterable<ParticipantWithRacesNumberDTO> getParticipantsWithRacesCountFromRace(SwimmingRace.DistanceType distance, SwimmingRace.Style style) throws SwimmingContestException {
        SwimmingRace swimmingRace = new SwimmingRace(distance, style);
        Request request = new Request.Builder().type(RequestType.SEARCH_PARTICIPANTS).data(swimmingRace).build();
        sendRequest(request);

        Response response = readResponse();
        if (response.type() == ResponseType.ERROR) {
            throw new SwimmingContestException(response.data().toString());
        }
        return response.data() != null ? (Iterable<ParticipantWithRacesNumberDTO>) response.data() : new ArrayList<ParticipantWithRacesNumberDTO>();
    }


    private boolean isUpdate(Response response) {
        return response.type() == ResponseType.NEW_PARTICIPATION;
    }


    private void startReader() {
        Thread thread = new Thread(new ReaderThread());
        thread.start();
    }


    private class ReaderThread implements Runnable {
        public void run() {
            while (!finished) {
                try {
                    Object response = input.readObject();
                    logger.debug("response received {}", response);

                    if (isUpdate((Response) response)) {
                        logger.debug("Update identified in ReaderThread: {}", response);
                        handleUpdate((Response) response); //daca e un update, il trimite la observer, se  actualizeaza datele
                    } else {
                        try {
                            queueResponses.put((Response) response); //e un rasp la o cerere, il pune in coada
                            logger.debug("Non-update response added to queue: {}", response);

                        } catch (InterruptedException e) {
                            logger.error(e);
                            logger.error(e.getStackTrace());
                            Thread.currentThread().interrupt();
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    logger.error("Reading error {}", e);
                }
            }
        }
    }


    private void handleUpdate(Response response) {
        if (response.type() == ResponseType.NEW_PARTICIPATION) {
            logger.debug("Notification new participation received {}", response);

            try {
                Iterable<RaceWithParticipantsNumberDTO> races = (Iterable<RaceWithParticipantsNumberDTO>) response.data();
                client.updateRaces(races);

            } catch (SwimmingContestException e) {
                logger.error(e);
            }
        }
    }

    private void sendRequest(Request request) throws SwimmingContestException {
        logger.debug("sending request {}", request);
        try {
            output.writeObject(request);
            output.flush();
        } catch (IOException e) {
            throw new SwimmingContestException("Error sending object. " + e.getMessage());
        }
    }

    private Response readResponse() throws SwimmingContestException {
        Response response = null;
        try {
            response = queueResponses.take();
        } catch (InterruptedException e) {
            logger.error(e);
            logger.error(e.getStackTrace());
        }
        return response;
    }
}
