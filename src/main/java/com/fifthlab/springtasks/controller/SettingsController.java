package com.fifthlab.springtasks.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class SettingsController {

    @GetMapping("/settings")
    public String showSettingsPage(Model model, HttpServletRequest request) {
        // Expose current URI to layout for active state
        model.addAttribute("currentUri", request.getRequestURI());
        return "settings";
    }
}
