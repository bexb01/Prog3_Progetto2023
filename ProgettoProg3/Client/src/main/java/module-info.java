module com.example.client {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.client to javafx.fxml;
    exports com.example.client;
    exports model;
    opens model to javafx.fxml;
    exports ui;
    opens ui to javafx.fxml;
}