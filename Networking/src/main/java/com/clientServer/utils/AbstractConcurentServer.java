package com.clientServer.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Socket;

public abstract class AbstractConcurentServer  extends AbstractServer {
    private static Logger logger = LogManager.getLogger(AbstractConcurentServer.class);
    public AbstractConcurentServer(final int port) {
        super(port);
        logger.debug("Concurrent Server started");
    }

    @Override
    protected void processRequest(Socket client) {
        Thread thread =  createWorker(client);
        thread.start();
    }

    protected abstract Thread createWorker(Socket client);
}
