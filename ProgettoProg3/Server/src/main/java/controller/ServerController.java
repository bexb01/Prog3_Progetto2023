package controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

public class ServerController implements Initializable {
    @FXML
    private ListView<String> messagesListView;

    @FXML
    private Button btnProva;

    private int i;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Add a list of messages to the ListView
        //List<String> messages = retrieveMessagesFromServer();
        //messagesListView.getItems().addAll(messages);
    }

    public void printLog(String s){
        messagesListView.getItems().add(s);
    }
    private List<String> retrieveMessagesFromServer() {
        // Replace this with a method that retrieves the messages from the server
        return List.of("Message 1", "Message 2", "Message 3");
    }
}
