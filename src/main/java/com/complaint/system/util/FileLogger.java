package com.complaint.system.util;

import java.io.FileWriter;
import java.io.IOException;

public class FileLogger {

    public static void log(String message) {
        try {
            FileWriter writer = new FileWriter("complaints_log.txt", true);

            writer.write(message + "\n");
            writer.close();

        } catch (IOException e) {
            System.out.println("Error writing to file");
        }
    }
}
