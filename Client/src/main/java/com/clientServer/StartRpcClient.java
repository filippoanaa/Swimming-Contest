package com.clientServer;

import com.clientServer.gui.AppController;
import com.clientServer.gui.LoginController;
import com.clientServer.rpcprotocol.ServicesRpcProxy;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Properties;

public class StartRpcClient extends Application {
    private static int defaultPort = 55555;
    private static String defaultHost = "localhost";

    public void start(Stage primaryStage) throws IOException {
        Properties clientProperties = new Properties();
        try{
            clientProperties.load(StartRpcClient.class.getResourceAsStream("/client.properties"));
            System.out.println("Client properties set.");
        } catch (IOException e) {
            System.err.println("Cannot find client.properties " + e.getMessage());
            return;
        }

        String serverIP = clientProperties.getProperty("server.host", defaultHost);
        int serverPort = defaultPort;

        try{
            serverPort = Integer.parseInt(clientProperties.getProperty("server.port", String.valueOf(defaultPort)));
        }catch (NumberFormatException e){
            System.err.println("Wrong port number "+e.getMessage());
            System.out.println("Using default port: "+ defaultPort);
        }

        System.out.println("Using server IP "+serverIP);
        System.out.println("Using server port "+serverPort);

        IServices server = new ServicesRpcProxy(serverIP, serverPort);

        FXMLLoader loginLoader = new FXMLLoader(StartRpcClient.class.getResource("/loginView.fxml"));
        Parent root = loginLoader.load();

        LoginController loginController = loginLoader.getController();
        loginController.setServer(server);

        FXMLLoader appLoader = new FXMLLoader(StartRpcClient.class.getResource("/appView.fxml"));
        Parent croot = appLoader.load();

        AppController appController = appLoader.getController();
        appController.setServer(server);



        loginController.setAppController(appController);
        loginController.setParent(croot);

        primaryStage.setTitle("Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
