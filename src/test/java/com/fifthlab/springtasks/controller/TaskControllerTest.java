package com.fifthlab.springtasks.controller;

import com.fifthlab.springtasks.model.Task;
import com.fifthlab.springtasks.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import com.fifthlab.springtasks.security.JwtUtil;
import com.fifthlab.springtasks.security.CustomUserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser
    public void testIndex() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    @WithMockUser
    public void testDashboard() throws Exception {
        when(taskService.findByStatus(null)).thenReturn(Arrays.asList(new Task(), new Task()));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("tasks"))
                .andExpect(model().attribute("activeStatus", "all"));
    }

    @Test
    @WithMockUser
    public void testSaveTask() throws Exception {
        mockMvc.perform(post("/tasks")
                .with(csrf())
                .param("title", "New Task")
                .param("description", "New Description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));
    }

    @Test
    @WithMockUser
    public void testSaveTaskValidationError() throws Exception {
        mockMvc.perform(post("/tasks")
                .with(csrf())
                .param("title", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("task-form"))
                .andExpect(model().hasErrors());
    }
}
