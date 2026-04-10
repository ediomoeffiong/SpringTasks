package com.fifthlab.springtasks.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice(basePackages = "com.fifthlab.springtasks.controller")
public class MvcExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model, HttpServletRequest request) {
        // Exclude REST API endpoints from this MVC exception handler
        if (request.getRequestURI().startsWith("/api")) {
            throw new RuntimeException(ex);
        }
        
        ex.printStackTrace();
        model.addAttribute("errorMessage", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred.");
        return "error";
    }
}
