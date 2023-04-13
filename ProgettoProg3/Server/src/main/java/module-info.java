module com.example.server {
    requires javafx.controls;
    requires javafx.fxml;
    requires gson;
    requires java.sql;


    opens com.example.server to javafx.fxml;
    opens model to javafx.fxml, gson;
    opens controller to javafx.fxml;
    exports controller;
    exports com.example.server;
    exports model;
}