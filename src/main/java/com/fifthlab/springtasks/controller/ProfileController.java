package com.fifthlab.springtasks.controller;

import com.fifthlab.springtasks.dto.DashboardResponseDTO;
import com.fifthlab.springtasks.model.User;
import com.fifthlab.springtasks.service.DashboardService;
import com.fifthlab.springtasks.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final DashboardService dashboardService;

    @GetMapping("/profile")
    public String showProfilePage(Model model, HttpServletRequest request) {
        String username = userService.getCurrentUsername();
        User user = userService.getCurrentUser().orElse(null);
        DashboardResponseDTO stats = dashboardService.getDashboardData(username);

        model.addAttribute("user", user);
        model.addAttribute("stats", stats);
        model.addAttribute("currentUri", request.getRequestURI());
        return "profile";
    }
}
