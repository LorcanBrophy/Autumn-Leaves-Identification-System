module com.example.dsa2_ca1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires jmh.core;
    requires java.desktop;
    requires javafx.swing;

    // javafx
    opens com.example.dsa2_ca1 to javafx.fxml;
    opens com.example.dsa2_ca1.controller to javafx.fxml;
    opens com.example.dsa2_ca1.main to javafx.fxml;

    // normal exports
    exports com.example.dsa2_ca1.model;
    exports com.example.dsa2_ca1.controller;
    exports com.example.dsa2_ca1.main;

    // JMH access
    exports com.example.dsa2_ca1.benchmark to jmh.core;
    exports com.example.dsa2_ca1.benchmark.jmh_generated to jmh.core;
}