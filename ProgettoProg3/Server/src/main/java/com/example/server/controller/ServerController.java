package com.example.server.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.Model;

public class ServerController implements Initializable {
    @FXML
    private ListView<String> messagesListView;

    @FXML
    private Button btnProva;
    private Model model;
    public void initModel(Model m){
        this.model = m;
        messagesListView.setItems(model.getLogList());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Add a list of messages to the ListView
        //List<String> messages = retrieveMessagesFromServer();
        //messagesListView.getItems().addAll(messages);
    }

    public void printLog(String s){

    }
    private List<String> retrieveMessagesFromServer() {
        // Replace this with a method that retrieves the messages from the server
        return List.of("Message 1", "Message 2", "Message 3");
    }
}
