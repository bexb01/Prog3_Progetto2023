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
import ui.ClientController;

import java.io.*;
import java.util.Random;

public class MailClient extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MailClient.class.getResource("client.fxml"));
        String username = generaUsername();
        InboxHandler inbx = new InboxHandler(username);
        ClientCommunication clientComm = new ClientCommunication(username, inbx);
        ClientController controller = new ClientController(username, clientComm, inbx);
        fxmlLoader.setController(controller);
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        stage.setTitle("Email client");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
        try {
            Thread cThread = new Thread(clientComm);
            cThread.setDaemon(true);
            cThread.start();
        }catch (Exception e){
            System.err.println("Error while creating client Thread: " + e.getMessage());
        }
        // Shutdown event
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {   //method exitonclose
            @Override
            public void handle(WindowEvent e) {
               /* try {
                    clientComm.shutdown();
                } catch (IOException ex) {
                    System.err.println("Error while exiting server: ");
                    throw new RuntimeException(ex);
                }
                Platform.exit();
                System.exit(0);
                */
            }
        });
    }

    private String generaUsername() {
        String[] firstNames = {"Emma", "Olivia", "Ava", "Isabella", "Sophia", "Mia", "Charlotte", "Amelia", "Harper", "Evelyn", "Giorgio"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson", "Moore", "Taylor", "Cussa"};
        String username = "";

        Random r = new Random();
        /*Switch up to set everyone different or everyone EmmaTaylor*/
        username = firstNames[r.nextInt(0, firstNames.length-1)] + "." + lastNames[r.nextInt(0, lastNames.length-1)] + "@unito.it";
        //username = "Emma.Taylor@unito.it";
        return username;
    }



    public static void main(String[] args) {
        launch();
    }


}