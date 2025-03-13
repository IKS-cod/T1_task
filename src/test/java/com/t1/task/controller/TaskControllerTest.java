package com.t1.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.t1.task.dto.TaskDto;
import com.t1.task.enums.TaskStatus;
import com.t1.task.exception.TaskNotFoundException;
import com.t1.task.kafka.KafkaTaskConsumer;
import com.t1.task.model.Task;
import com.t1.task.repository.TaskRepository;
import com.t1.task.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private KafkaTaskConsumer kafkaTaskConsumer;
    @MockBean
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void createTaskValidInputReturnsCreatedTaskTest() throws Exception {
        // Arrange
        TaskDto taskDto = new TaskDto();
        taskDto.setTitle("Test Task");
        taskDto.setDescription("Test Description");
        taskDto.setUserId(1L);
        taskDto.setStatus(TaskStatus.IN_PROGRESS);
        // Act
        // Assert
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void getTaskExistingIdReturnsTaskTest() throws Exception {
        // Arrange
        Task taskForDb = new Task();
        taskForDb.setTitle("Test Task");
        taskForDb.setDescription("Test Description");
        taskForDb.setStatus(TaskStatus.IN_PROGRESS);
        taskForDb.setUserId(1L);
        taskRepository.save(taskForDb);
        List<Task> taskListFromDb = taskRepository.findAll();
        Long id = taskListFromDb.get(0).getId();

        // Act
        // Assert
        mockMvc.perform(get("/tasks/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void getTaskNonExistingIdReturnsNotFoundTest() throws Exception {
        long id = 999L;
        mockMvc.perform(get("/tasks/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTaskExistingIdUpdatesStatusTaskAndCheckingCalledKafkaTaskProducerTest() throws Exception {

        // Save initial task
        Task taskForDb = new Task();
        taskForDb.setTitle("Initial Task");
        taskForDb.setDescription("Initial Description");
        taskForDb.setStatus(TaskStatus.NEW);
        taskForDb.setUserId(1L);

        taskRepository.save(taskForDb);

        List<Task> taskListFromDb = taskRepository.findAll();
        assertEquals(1, taskListFromDb.size());
        Long id = taskListFromDb.get(0).getId();

        // Updated task data
        TaskDto updatedTask = new TaskDto();
        updatedTask.setTitle("Updated Task");
        updatedTask.setDescription("Updated Description");
        updatedTask.setUserId(2L);
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);

        mockMvc.perform(put("/tasks/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isOk());

        // Verify the task is updated
        Task taskFromDb = taskRepository.findById(id).orElseThrow(() ->
                new TaskNotFoundException("Task not found with ID: " + id));
        assertEquals(updatedTask.getTitle(), taskFromDb.getTitle());
        assertEquals(updatedTask.getDescription(), taskFromDb.getDescription());
        assertEquals(updatedTask.getStatus(), taskFromDb.getStatus());
        //check called kafka and called method sendSimpleEmail
        // wait (10 sec)
        await().atMost(10, SECONDS).untilAsserted(() -> {
            // Проверка вызова метода sendSimpleEmail
            verify(notificationService).sendSimpleEmail(
                    eq("Изменение статуса Task c id= " + id),
                    eq("Статус Task стал - " + taskFromDb.getStatus())
            );
        });

    }

    @Test
    void updateTaskExistingIdWithoutUpdatesStatusTaskAndCheckingNeverCalledKafkaTaskProducerTest() throws Exception {
        // Save initial task
        Task taskForDb = new Task();
        taskForDb.setTitle("Initial Task");
        taskForDb.setDescription("Initial Description");
        taskForDb.setStatus(TaskStatus.NEW);
        taskForDb.setUserId(1L);

        taskRepository.save(taskForDb);

        List<Task> taskListFromDb = taskRepository.findAll();
        assertEquals(1, taskListFromDb.size());
        Long id = taskListFromDb.get(0).getId();

        // Updated task data
        TaskDto updatedTask = new TaskDto();
        updatedTask.setTitle("Updated Task");
        updatedTask.setDescription("Updated Description");
        updatedTask.setUserId(2L);
        updatedTask.setStatus(TaskStatus.NEW);

        mockMvc.perform(put("/tasks/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isOk());

        // Verify the task is updated
        Task taskFromDb = taskRepository.findById(id).orElseThrow(() ->
                new TaskNotFoundException("Task not found with ID: " + id));
        assertEquals(updatedTask.getTitle(), taskFromDb.getTitle());
        assertEquals(updatedTask.getDescription(), taskFromDb.getDescription());
        assertEquals(updatedTask.getStatus(), taskFromDb.getStatus());
        //check called kafka and called method sendSimpleEmail
        // wait (10 sec)
        await().atMost(10, SECONDS).untilAsserted(() -> {
            // Проверка вызова метода sendSimpleEmail
            verify(notificationService, never()).sendSimpleEmail(
                    any(),
                    any()
            );
        });

    }

    @Test
    void updateTaskNonExistingIdReturnsNotFoundTest() throws Exception {
        TaskDto taskDto = new TaskDto();
        taskDto.setTitle("Updated Task");
        taskDto.setDescription("Updated Description");
        taskDto.setUserId(2L);
        taskDto.setStatus(TaskStatus.IN_PROGRESS);

        long id = 999L;

        mockMvc.perform(put("/tasks/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTaskExistingIdDeletesTaskTest() throws Exception {
        // Arrange
        Task taskForDb = new Task();
        taskForDb.setTitle("Test Task");
        taskForDb.setDescription("Test Description");
        taskForDb.setStatus(TaskStatus.IN_PROGRESS);
        taskForDb.setUserId(1L);
        taskRepository.save(taskForDb);
        List<Task> taskListFromDb = taskRepository.findAll();
        Long id = taskListFromDb.get(0).getId();

        // Delete the task
        mockMvc.perform(delete("/tasks/" + id))
                .andExpect(status().isOk());

        // Verify the task is deleted
        Optional<Task> taskFromDb = taskRepository.findById(id);
        assertTrue(taskFromDb.isEmpty());
    }

    @Test
    void deleteTaskNonExistingIdReturnsNotFoundTest() throws Exception {
        long id = 999L;
        mockMvc.perform(delete("/tasks/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllTasksTasksExistReturnsListOfTasksTest() throws Exception {
        // Save initial tasks
        Task taskForDb1 = new Task();
        taskForDb1.setTitle("Test Task 1");
        taskForDb1.setDescription("Test Description");
        taskForDb1.setStatus(TaskStatus.IN_PROGRESS);
        taskForDb1.setUserId(1L);

        Task taskForDb2 = new Task();
        taskForDb2.setTitle("Test Task 2");
        taskForDb2.setDescription("Test Description");
        taskForDb2.setStatus(TaskStatus.IN_PROGRESS);
        taskForDb2.setUserId(2L);

        taskRepository.save(taskForDb1);
        taskRepository.save(taskForDb2);
        List<Task> taskListFromDb = taskRepository.findAll();
        assertEquals(2, taskListFromDb.size());

        // Retrieve all tasks
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Test Task 1"))
                .andExpect(jsonPath("$[1].title").value("Test Task 2"));
    }

    @Test
    void getAllTasksNoTasksExistReturnsEmptyListTest() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

}
