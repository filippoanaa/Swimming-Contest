package com.clientServer.utils;

import com.clientServer.rpcprotocol.ClientRpcWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.clientServer.IServices;

import java.net.Socket;

public class RpcConcurrentServer  extends AbstractConcurentServer{
    private IServices server;
    private static Logger logger = LogManager.getLogger(RpcConcurrentServer.class);

    public RpcConcurrentServer(int port, IServices server) {
        super(port);
        this.server = server;
        logger.info("RpcConcurrentServer started");
    }

    @Override
    protected Thread createWorker(Socket client) {
        ClientRpcWorker worker = new ClientRpcWorker(server, client);
        return new Thread(worker);
    }
}
