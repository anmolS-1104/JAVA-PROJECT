package com.complaint.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

// 🔹 Added the exclude parameter to stop the DataSource error
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = "com.complaint.system")
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);

        System.out.println("--------------------------------------------------");
        System.out.println("✅ Backend server started successfully!");
        System.out.println("🚀 Ready for JavaFX Client connection on port 8080");
        System.out.println("--------------------------------------------------");
    }
}