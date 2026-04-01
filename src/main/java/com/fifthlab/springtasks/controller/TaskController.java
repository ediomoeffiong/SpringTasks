package com.fifthlab.springtasks.controller;

import com.fifthlab.springtasks.model.Task;
import com.fifthlab.springtasks.service.TaskService;
import com.fifthlab.springtasks.service.DashboardService;
import com.fifthlab.springtasks.model.DashboardResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

@Controller
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final DashboardService dashboardService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboardOverview(Model model) {
        String username = dashboardService.getCurrentUsername();
        DashboardResponseDTO dashboardData = dashboardService.getDashboardData(username);
        model.addAttribute("dashboardData", dashboardData);
        model.addAttribute("userName", username);
        return "dashboard-overview";
    }

    @GetMapping("/my-tasks")
    public String myTasks(Model model) {
        return "my-tasks";
    }

    @GetMapping("/calendar")
    public String calendar(Model model) {
        return "calendar";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        return "categories";
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        return "analytics";
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        return "notifications";
    }

    @GetMapping("/tasks")
    public String dashboard(@RequestParam(required = false) String status, Model model) {
        Boolean completed = null;
        if ("completed".equalsIgnoreCase(status)) {
            completed = true;
        } else if ("pending".equalsIgnoreCase(status)) {
            completed = false;
        }

        model.addAttribute("tasks", taskService.findByStatus(completed));
        model.addAttribute("activeStatus", status != null ? status : "all");
        return "dashboard";
    }

    @GetMapping("/tasks/new")
    public String newTaskForm(Model model) {
        model.addAttribute("task", new Task());
        return "task-form";
    }

    @PostMapping("/tasks")
    public String saveTask(@Valid @ModelAttribute("task") Task task, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "task-form";
        }
        taskService.save(task);
        return "redirect:/tasks";
    }

    @GetMapping("/tasks/edit/{id}")
    public String editTaskForm(@PathVariable @NonNull Long id, Model model) {
        Task task = taskService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task Id:" + id));
        model.addAttribute("task", task);
        return "task-form";
    }

    @PostMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable @NonNull Long id) {
        taskService.deleteById(id);
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/toggle/{id}")
    public String toggleTask(@PathVariable @NonNull Long id) {
        taskService.toggleStatus(id);
        return "redirect:/tasks";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser() {
        // Mock registration: redirect to login with success message
        return "redirect:/login?signupSuccess";
    }
}
