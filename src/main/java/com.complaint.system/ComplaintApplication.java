package com.complaint.system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ComplaintApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // This looks for the dashboard.fxml we put in src/main/resources
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("ICRS - Intelligent Dashboard");
        primaryStage.setScene(new Scene(root, 700, 500));

        // This makes the window pop up!
        primaryStage.show();
    }

    public static void main(String[] args) {
        // This starts the JavaFX lifecycle instead of the Spring Boot one
        launch(args);
    }
}