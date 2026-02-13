module com.example.dsa2_ca1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.dsa2_ca1 to javafx.fxml;
    exports com.example.dsa2_ca1.model;
    exports com.example.dsa2_ca1.controller;
    opens com.example.dsa2_ca1.controller to javafx.fxml;
    exports com.example.dsa2_ca1.main;
    opens com.example.dsa2_ca1.main to javafx.fxml;
}