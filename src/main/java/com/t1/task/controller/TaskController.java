package com.t1.task.controller;

import com.t1.task.dto.TaskDto;
import com.t1.task.dto.TaskWithoutIdDto;
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
    public TaskWithoutIdDto createTask(@RequestBody TaskWithoutIdDto taskWithoutIdDto) {
        return taskService.createTask(taskWithoutIdDto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение задачи по ID")
    public TaskDto getTask(@PathVariable long id) {
        return taskService.getTask(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновление данных задачи по ID")
    public void updateTask(@PathVariable long id, @RequestBody TaskWithoutIdDto taskWithoutIdDto) {
        taskService.updateTask(id, taskWithoutIdDto);
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
