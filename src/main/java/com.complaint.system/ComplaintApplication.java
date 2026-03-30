package com.complaint.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ComplaintApplication {
    public static void main(String[] args) {
        SpringApplication.run(ComplaintApplication.class, args);
    }

    // This "EventListener" waits until the server is 100% ready
    @EventListener(ApplicationReadyEvent.class)
    public void launchBrowser() {
        System.setProperty("java.awt.headless", "false");
        String url = "http://localhost:8080/";

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
                System.out.println("🚀 AI Dashboard launched successfully at: " + url);
            }
        } catch (Exception e) {
            System.out.println("❌ Could not auto-launch browser. Please visit: " + url);
        }
    }
}