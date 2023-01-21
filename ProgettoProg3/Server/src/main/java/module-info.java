module com.example.server {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.server to javafx.fxml;
    exports com.example.server;
    exports model;
    opens model to javafx.fxml;
    opens com.example.server.controller to javafx.fxml;
    exports com.example.server.controller;
}