module com.example.server {
    requires javafx.controls;
    requires javafx.fxml;
    requires gson;
    requires java.sql;

    opens model to javafx.fxml, gson;
    opens com.example.server to javafx.fxml;
    opens com.example.server.controller to javafx.fxml;
    exports com.example.server;
    exports model;
    exports com.example.server.controller;
}