package com.complaint.system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ComplaintApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Load the Login Screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = loader.load();

        // 2. Set the Title and Scene (using your 700x650 dark theme size)
        primaryStage.setTitle("ICRS - Login");
        primaryStage.setScene(new Scene(root, 700, 650));

        // 3. Show the window
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}