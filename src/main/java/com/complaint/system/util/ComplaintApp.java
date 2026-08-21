package com.complaint.system.util;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class ComplaintApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL fxml = getClass().getResource("/login.fxml");
        if (fxml == null) {
            fxml = getClass().getResource("/com/complaint/system/login.fxml");
        }
        if (fxml == null) {
            throw new IllegalStateException("login.fxml not found. Ensure it is placed inside src/main/resources/");
        }

        FXMLLoader loader = new FXMLLoader(fxml);
        Parent root = loader.load();

        primaryStage.setTitle("Complaint Management System - Login");
        primaryStage.setScene(new Scene(root, 450, 400));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}