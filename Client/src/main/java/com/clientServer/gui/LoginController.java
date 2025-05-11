package com.clientServer.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.clientServer.entities.User;
import com.clientServer.IServices;

public class LoginController{
    private IServices server;
    private User currentUser;
    private AppController appController;
    private Parent root;

    Logger logger = LogManager.getLogger(LoginController.class);


    @FXML
    private TextField usernameText;
    @FXML
    private PasswordField passwordText;

    public void setServer(IServices server) {
        logger.debug("Set server in login controller");
        this.server = server;
    }

    public void setAppController(AppController appController) {
        logger.debug("Set app controller in login controller");
        this.appController = appController;
    }

    public void setParent(Parent p) {
        this.root = p;
    }


    @FXML
    public void handleLogInButton(ActionEvent event) {
        String username = usernameText.getText();
        String password = passwordText.getText();

        if(username.isEmpty() || password.isEmpty()) {
            MyAlerts.showErrorAlert("Both username and password are required");
            clearFields();
            return;
        }

        currentUser = new User(username, password);
        try{
            server.login(currentUser, appController);
            logger.debug("AppController registered as observer");

            Stage stage = new Stage();
            stage.setTitle("Welcome " + currentUser.getUsername());
            stage.setScene(new Scene(root));
            stage.show();

//            stage.setOnCloseRequest(ev -> {
//                appController.handleLogout();
//                System.exit(0);
//            });

            appController.setUser(currentUser);
            appController.setServer(server);
            ((Node)(event.getSource())).getScene().getWindow().hide();




        }catch(Exception e){
            MyAlerts.showErrorAlert(e.getMessage());
            clearFields();
        }
    }

    private void clearFields(){
        usernameText.clear();
        passwordText.clear();
    }



}
