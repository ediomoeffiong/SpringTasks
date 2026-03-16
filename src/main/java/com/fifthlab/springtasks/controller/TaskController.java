package com.fifthlab.springtasks.controller;

import com.fifthlab.springtasks.model.Task;
import com.fifthlab.springtasks.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/")
    public String index() {
        return "index";
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
    public String editTaskForm(@PathVariable Long id, Model model) {
        Task task = taskService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task Id:" + id));
        model.addAttribute("task", task);
        return "task-form";
    }

    @PostMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteById(id);
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/toggle/{id}")
    public String toggleTask(@PathVariable Long id) {
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
