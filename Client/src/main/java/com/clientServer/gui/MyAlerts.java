package com.clientServer.gui;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class MyAlerts {
    public static void showConfirmationAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.setContentText(message);
        alert.showAndWait();

    }

    public static void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showWarningAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.setContentText(message);
        alert.showAndWait();
    }
}