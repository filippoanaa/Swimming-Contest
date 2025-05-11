package com.clientServer.gui;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.clientServer.dto.ParticipantWithRacesNumberDTO;
import com.clientServer.dto.ParticipationDTO;
import com.clientServer.dto.RaceWithParticipantsNumberDTO;
import com.clientServer.entities.Participant;
import com.clientServer.entities.SwimmingRace;
import com.clientServer.entities.User;
import com.clientServer.IObserver;
import com.clientServer.IServices;
import com.clientServer.SwimmingContestException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class AppController implements Initializable, IObserver {
    private IServices server;
    private User user;

    private static Logger logger = LogManager.getLogger(AppController.class);



    public AppController(IServices server) {
        this.server = server;
        logger.debug("AppController with server param");

    }

    public AppController() {
        logger.debug("AppController no-arg constructor");
    }

    private ObservableList<RaceWithParticipantsNumberDTO> swimmingRacesModel = FXCollections.observableArrayList();
    private ObservableList<ParticipantWithRacesNumberDTO> participantsModel = FXCollections.observableArrayList();

    @FXML
    private TableView<RaceWithParticipantsNumberDTO> swimmingRacesTable = new TableView<>();
    @FXML
    TableColumn<RaceWithParticipantsNumberDTO, SwimmingRace.DistanceType> distanceColumn = new TableColumn<>("Distance");
    @FXML
    TableColumn<RaceWithParticipantsNumberDTO, SwimmingRace.Style> styleColumn = new TableColumn<>("Style");
    @FXML
    TableColumn<RaceWithParticipantsNumberDTO, Integer> numberOfParticipantsColumn = new TableColumn<>("Number of Participants");

    @FXML
    private TableView<ParticipantWithRacesNumberDTO> participantsTable = new TableView<>();
    @FXML
    TableColumn<ParticipantWithRacesNumberDTO, String> participantNameColumn = new TableColumn<>("Name");
    @FXML
    private TableColumn<ParticipantWithRacesNumberDTO, Integer> participantAgeColumn = new TableColumn<>("Age");
    @FXML
    private TableColumn<ParticipantWithRacesNumberDTO, Integer> numberOfRaceColumn = new TableColumn<>("Number of Races");


    @FXML
    private ComboBox<SwimmingRace.DistanceType> comboBoxDistance;
    @FXML
    private ComboBox<SwimmingRace.Style> comboBoxStyle;

    @FXML
    private TextField participantNameTextField;
    @FXML
    private TextField participantAgeTextField;


    public void setServer(IServices server) {
        logger.debug("Am setat serverul");
        this.server = server;

    }

    public void setUser(User user) {
        this.user = user;
        setupSwimmingRacesTable();
        setupComboBoxes();
    }

    private void setupSwimmingRacesTable() {
        try{
            Iterable<RaceWithParticipantsNumberDTO> races = server.getRacesWithParticipants();
            List<RaceWithParticipantsNumberDTO> racesList = StreamSupport.stream(races.spliterator(), false).collect(Collectors.toList());

            distanceColumn.setCellValueFactory(cellData ->{
                if(cellData!=null){
                    String distance = cellData.getValue().getSwimmingRace().getDistanceType().toString();
                    return new SimpleObjectProperty(distance);
                }
                return null;
            });
            styleColumn.setCellValueFactory(cellData ->{
                if(cellData!=null){
                    String style = cellData.getValue().getSwimmingRace().getStyle().toString();
                    return new SimpleObjectProperty(style);
                }
                return null;
            });
            numberOfParticipantsColumn.setCellValueFactory(cellData ->{
                if(cellData!=null){
                    Integer numberOfParticipants = cellData.getValue().getParticipantsNumber();
                    return new SimpleObjectProperty(numberOfParticipants);
                }
                return null;
            });


            swimmingRacesModel.setAll(racesList);
            swimmingRacesTable.setItems(swimmingRacesModel);


        } catch (SwimmingContestException e) {
            MyAlerts.showErrorAlert(e.getMessage());
        }
        swimmingRacesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    }

    private void setupParticipantsTable(List<ParticipantWithRacesNumberDTO> participants) {
        participantNameColumn.setCellValueFactory(cellData ->{
            if(cellData!=null){
                String name = cellData.getValue().getParticipant().getName();
                return new SimpleObjectProperty<>(name);
            }
            return null;
        });
        participantAgeColumn.setCellValueFactory(cellData ->{
            if(cellData!=null){
                Integer age = cellData.getValue().getParticipant().getAge();
                return new SimpleObjectProperty<>(age);
            }
            return null;
        });
        numberOfRaceColumn.setCellValueFactory(cellData ->{
            if(cellData!=null){
                Integer numberOfRaces = cellData.getValue().getRacesNumber();
                return new SimpleObjectProperty<>(numberOfRaces);
            }
            return null;
        });
        participantsModel.setAll(participants);
        participantsTable.setItems(participantsModel);
    }

    private void setupComboBoxes() {
        comboBoxDistance.setItems(FXCollections.observableArrayList(SwimmingRace.DistanceType.values()));
        comboBoxStyle.setItems(FXCollections.observableArrayList(SwimmingRace.Style.values()));
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        swimmingRacesTable.setItems(swimmingRacesModel);
        participantsTable.setItems(participantsModel);

    }

    @FXML
    private void handleSearchParticipantsFromSwimmingRace(ActionEvent event) {
        SwimmingRace.DistanceType distance = comboBoxDistance.getSelectionModel().getSelectedItem();
        SwimmingRace.Style style = comboBoxStyle.getSelectionModel().getSelectedItem();

        if (distance == null || style == null) {
            MyAlerts.showErrorAlert("Select distance and style from the combo boxes in order to filter!");
            clearFields();
            return;
        }
        try{
            Iterable<ParticipantWithRacesNumberDTO> participants = server.getParticipantsWithRacesCountFromRace(distance, style);
            List<ParticipantWithRacesNumberDTO> participantsList = StreamSupport.stream(participants.spliterator(), false).collect(Collectors.toList());

            if(participantsList.isEmpty()){
                MyAlerts.showConfirmationAlert("No participants enrolled yet!");
                participantsTable.getItems().clear();
                return;
            }

            setupParticipantsTable(participantsList);
            clearFields();

        } catch (SwimmingContestException e) {
            logger.error("Failed to search the participants from races: {}, {}. {}", distance, style, e.getMessage());
            MyAlerts.showErrorAlert(e.getMessage());
        }



    }

    @FXML
    private void handleAddParticipantToSwimmingRaces(ActionEvent event) throws SwimmingContestException {
        if (participantNameTextField.getText().isEmpty() || participantAgeTextField.getText().isEmpty() || participantAgeTextField.getText().isEmpty()) {
            MyAlerts.showErrorAlert("Name and age are required for the participation!");
            clearFields();
            return;
        }
        String name = participantNameTextField.getText();
        int age;
        try{
            age = Integer.parseInt(participantAgeTextField.getText());
        }catch (NumberFormatException e){
            MyAlerts.showErrorAlert("Please enter a valid number for the age!");
            clearFields();
            return;
        }

        if(age < 0){
            MyAlerts.showErrorAlert("Please enter a valid age!");
            clearFields();
        }


        Participant participant = new Participant(name, age);
        ObservableList<RaceWithParticipantsNumberDTO> swimmingRaces = swimmingRacesTable.getSelectionModel().getSelectedItems();
        List<SwimmingRace> swimmingRacesList = new ArrayList<>();

        for(RaceWithParticipantsNumberDTO race : swimmingRaces){
            swimmingRacesList.add(race.getSwimmingRace());
        }

        if (swimmingRaces.isEmpty()) {
            MyAlerts.showErrorAlert("Select the swimmingRaces from the table");
            return;
        }


        server.createParticipation(new ParticipationDTO(participant, swimmingRacesList));
        MyAlerts.showConfirmationAlert("Participations successfully created");


        clearFields();


    }

    private void clearFields() {
        participantNameTextField.clear();
        participantAgeTextField.clear();

        swimmingRacesTable.getSelectionModel().clearSelection();
        comboBoxDistance.getSelectionModel().clearSelection();
        comboBoxStyle.getSelectionModel().clearSelection();

    }


    @Override
    public void updateRaces(Iterable<RaceWithParticipantsNumberDTO> races) throws SwimmingContestException {
        logger.debug("updateRaces called on AppController");
        Platform.runLater(() -> {
            List<RaceWithParticipantsNumberDTO> racesList = StreamSupport.stream(races.spliterator(), false).collect(Collectors.toList());
            swimmingRacesModel.setAll(FXCollections.observableArrayList(racesList));
            logger.debug("swimmingRacesModel updated");
        });
    }


    public void handleLogout(ActionEvent event) {
        try{
            server.logout(user, this);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.close();

        }catch (SwimmingContestException e){
            MyAlerts.showErrorAlert(e.getMessage());

        }
    }
}
