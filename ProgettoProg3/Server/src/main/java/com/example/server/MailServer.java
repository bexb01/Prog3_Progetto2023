package com.example.server;

import controller.ServerController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class MailServer extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        //FmxLLoader
        FXMLLoader fxmlLoader = new FXMLLoader(MailServer.class.getResource("server.fxml"));
        ServerController controller = new ServerController();
        fxmlLoader.setController(controller);
        //Scene set-up
        Scene scene = new Scene(fxmlLoader.load());
        stage.setResizable(false);
        stage.setTitle("Server UI");
        stage.setScene(scene);
        stage.show();

        try {
            Server server = new Server(8189, controller);
            Thread pServer = new Thread(server);
            pServer.setDaemon(true);
            pServer.start();
            //Shutdown the server on close button click
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent e) {
                    try {
                        server.shutdownServer();
                    } catch (IOException ex) {
                        System.err.println("Error while exiting server: ");
                        throw new RuntimeException(ex);
                    }
                    Platform.exit();
                    System.exit(0);
                }
            });
        }catch (Exception e){
            System.err.println("Error while creating server thread: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}