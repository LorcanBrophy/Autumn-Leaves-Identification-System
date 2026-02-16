package com.example.dsa2_ca1.main;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {

        // load the fxml
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("/com/example/dsa2_ca1/view.fxml"));

        // put the loaded fxml into a scene
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);

        // attach the stylesheet
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/example/dsa2_ca1/style.css")).toExternalForm());

        // window setup
        stage.setTitle("Autumn Leaf Detector");
        stage.setScene(scene);

        stage.show();
    }
}
