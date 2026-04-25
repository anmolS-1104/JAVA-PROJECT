package com;

import com.icrs.controller.UserController;
import com.icrs.model.User;

public class Main {
    public static void main(String[] args) {

        UserController userController = new UserController();

        // 1. Register a user
        userController.register("Alice", "alice@email.com", "pass123", "9999999999", "USER");

        // 2. Login
        User loggedInUser = userController.login("alice@email.com", "pass123", "USER");

        if (loggedInUser != null) {

            // 3. Submit complaints
            userController.submitComplaint("payment failed", "NORMAL", "FinanceDepartment", loggedInUser.getId());
            userController.submitComplaint("delivery not received", "NORMAL", "LogisticsDepartment", loggedInUser.getId());

            // 4. View complaints
            userController.viewMyComplaints(loggedInUser.getId());

            // 5. Delete complaint with ID 1
            userController.deleteMyComplaint(1, loggedInUser.getId());

            // 6. View again to confirm deletion
            userController.viewMyComplaints(loggedInUser.getId());
        }
    }
}