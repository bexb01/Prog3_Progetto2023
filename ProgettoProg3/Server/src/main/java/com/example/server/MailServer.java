package com.example.server;

import controller.ServerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
            Thread pServer = new Thread(new Server(8189, controller));
            pServer.setDaemon(true);
            pServer.start();
        }catch (Exception e){}
    }

    public static void main(String[] args) {
        launch();
    }
}