package com.fifthlab.springtasks.controller;

import com.fifthlab.springtasks.model.DashboardResponseDTO;
import com.fifthlab.springtasks.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardRestController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponseDTO> getDashboardData() {
        String username = dashboardService.getCurrentUsername();
        DashboardResponseDTO data = dashboardService.getDashboardData(username);
        return ResponseEntity.ok(data);
    }
}
