package com.t1.task.controller;

import com.t1.task.dto.TaskDto;
import com.t1.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Создание новой задачи")
    public TaskDto createTask(@RequestBody TaskDto taskDto) {
        return taskService.createTask(taskDto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение задачи по ID")
    public TaskDto getTask(@PathVariable long id) {
        return taskService.getTask(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновление данных задачи по ID")
    public void updateTask(@PathVariable long id, @RequestBody TaskDto taskDto) {
        taskService.updateTask(id, taskDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление задачи по ID")
    public void deleteTask(@PathVariable long id) {
        taskService.deleteTask(id);
    }

    @GetMapping
    @Operation(summary = "Получение списка всех задач")
    public Collection<TaskDto> getAllTask() {
        return taskService.findAllTask();
    }

}
