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
        SpringApplication.run(ComplaintApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void launchBrowser() {
        System.setProperty("java.awt.headless", "false");
        String url = "http://localhost:8080/";
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
                System.out.println("Dashboard launched at: " + url);
            }
        } catch (Exception e) {
            System.out.println("Could not launch browser. Visit: " + url);
        }
    }
}