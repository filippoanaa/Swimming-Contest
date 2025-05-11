package com.clientServer.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;



public abstract class AbstractServer {
    private int port;
    private ServerSocket serverSocket = null;
    private static Logger logger = LogManager.getLogger(AbstractServer.class);

    public AbstractServer(int port) {
        this.port = port;
    }

    public void start() throws ServerException {
        try{
            serverSocket = new ServerSocket(port);
            while(true){
                logger.info("Waiting for a client on port {} ", port);
                Socket client = serverSocket.accept();
                logger.info("Accepted a client on port {}", port);
                processRequest(client);
            }

        }catch (IOException e){
            throw new ServerException(e.getMessage());
        }finally {
            stop();
        }
    }

    public void stop() throws ServerException {
        try{
            serverSocket.close();
        }catch (IOException e){
            throw new ServerException("Closing server error: " + e.getMessage());
        }
    }

    protected abstract void processRequest(Socket client);
}
