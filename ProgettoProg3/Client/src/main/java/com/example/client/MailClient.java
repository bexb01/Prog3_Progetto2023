package com.example.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.ClientCommunication;
import model.InboxHandler;
import controller.ClientController;

import java.io.*;
import java.util.Random;

public class MailClient extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MailClient.class.getResource("client.fxml"));
        String username = generaUsername();
        InboxHandler inbx = new InboxHandler(username);
        ClientController controller = new ClientController(username, inbx);
        fxmlLoader.setController(controller);
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        stage.setTitle("Email client");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                Platform.exit();
                System.exit(0);
            }
        });
    }

    /**
     * Genera nomi utente per il client
     */
    private String generaUsername() {
        String[] Names = {"Jake.Peralta", "Alphonse.Elric", "Amy.Santiago", "Chuck.Bartowski", "Sarah.Walker", "Tony.Stark", "Edward.Elric", "Pepper.Potts"};
        String username = "";
        Random r = new Random();
        username = Names[r.nextInt(0, Names.length-1)] + "@unito.it";
        //username = "Alphonse.Elric@unito.it";
        return username;
    }



    public static void main(String[] args) {
        launch();
    }


}