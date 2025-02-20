package com.t1.task.service;

import com.t1.task.aspect.annotation.CustomExceptionHandling;
import com.t1.task.aspect.annotation.CustomExecutionTime;
import com.t1.task.aspect.annotation.CustomLoggingFinishedMethod;
import com.t1.task.aspect.annotation.CustomLoggingStartMethod;
import com.t1.task.dto.TaskDto;
import com.t1.task.dto.TaskWithoutIdDto;
import com.t1.task.exception.TaskNotFoundException;
import com.t1.task.mapper.TaskMapper;
import com.t1.task.model.Task;
import com.t1.task.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    @CustomLoggingStartMethod
    @CustomLoggingFinishedMethod
    @CustomExecutionTime
    public TaskWithoutIdDto createTask(TaskWithoutIdDto taskWithoutIdDto) {
        Task taskForDb = taskMapper.toTask(taskWithoutIdDto);
        taskForDb.setId(null);
        Task taskSaveInDb = taskRepository.save(taskForDb);
        return taskMapper.toTaskWithoutIdDto(taskSaveInDb);
    }

    @CustomLoggingStartMethod
    @CustomLoggingFinishedMethod
    @CustomExceptionHandling
    @CustomExecutionTime
    public TaskDto getTask(long id) {
        Task taskFromDb = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));
        return taskMapper.toTaskDto(taskFromDb);
    }

    @CustomLoggingStartMethod
    @CustomLoggingFinishedMethod
    @CustomExceptionHandling
    @CustomExecutionTime
    public void updateTask(long id, TaskWithoutIdDto taskWithoutIdDto) {
        Task taskFromDb = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));
        taskFromDb.setTitle(taskWithoutIdDto.getTitle());
        taskFromDb.setDescription(taskWithoutIdDto.getDescription());
        taskFromDb.setUserId(taskWithoutIdDto.getUserId());
        taskRepository.save(taskFromDb);
    }

    @CustomLoggingStartMethod
    @CustomLoggingFinishedMethod
    @CustomExceptionHandling
    @CustomExecutionTime
    public void deleteTask(long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task not found with ID: " + id);
        }
        taskRepository.deleteById(id);
    }

    @CustomLoggingStartMethod
    @CustomLoggingFinishedMethod
    @CustomExecutionTime
    public Collection<TaskDto> findAllTask() {
        List<Task> taskListFromDb = taskRepository.findAll();
        List<TaskDto> taskDtoList = new ArrayList<>();
        for (Task e : taskListFromDb) {
            taskDtoList.add(taskMapper.toTaskDto(e));
        }
        return taskDtoList;
    }
}
