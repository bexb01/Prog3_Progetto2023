package com.example.server;

import com.example.server.controller.ServerController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.Model;

import java.io.IOException;

public class MailServer extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        //FmxLLoader
        FXMLLoader fxmlLoader = new FXMLLoader(MailServer.class.getResource("server.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        //server set up
        Model model = new Model();
        model.startServer();
        ServerController controller = fxmlLoader.getController();
        controller.initModel(model);

            //Shutdown the server on close button click
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent e) {
                    try {
                        model.getServer().shutdownServer();
                    } catch (IOException ex) {
                        System.err.println("Error while exiting server: ");
                        throw new RuntimeException(ex);
                    }
                    Platform.exit();
                    System.exit(0);
                }
            });

        //Stage set-up
        stage.setResizable(false);
        stage.setTitle("Server UI");
        stage.setScene(scene);
        stage.show();


    }

    public static void main(String[] args) {
        launch();
    }
}