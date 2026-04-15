package com.complaint.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import java.awt.Desktop;
import java.net.URI;

@SpringBootApplication
@ComponentScan(basePackages = "com")     // Fixing the CORS error
public class ComplaintApplication {

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