package com.complaint.system;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ComplaintController {

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("title", "Intelligent Complaint System");
        return "dashboard"; // This looks for dashboard.html in templates
    }
}