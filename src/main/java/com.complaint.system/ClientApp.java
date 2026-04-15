package com.complaint.system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // This tells JavaFX to load the login UI design from resources folder
        Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));

        primaryStage.setTitle("ICRS - Login");
        primaryStage.setScene(new Scene(root, 700, 650));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}