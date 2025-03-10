package com.t1.task.service;

import com.t1.task.aspect.annotation.CustomExceptionHandling;
import com.t1.task.aspect.annotation.CustomExecutionTime;
import com.t1.task.aspect.annotation.CustomLoggingFinishedMethod;
import com.t1.task.aspect.annotation.CustomLoggingStartMethod;
import com.t1.task.dto.TaskDto;
import com.t1.task.dto.TaskUpdateStatusDto;
import com.t1.task.enums.TaskStatus;
import com.t1.task.exception.TaskNotFoundException;
import com.t1.task.kafka.KafkaTaskProducer;
import com.t1.task.mapper.TaskMapper;
import com.t1.task.model.Task;
import com.t1.task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ValidationService validationService;
    private final KafkaTaskProducer kafkaTaskProducer;
    @Value("${t1.kafka.topic.task_update}")
    private String topic;

    public TaskService(TaskRepository taskRepository,
                       TaskMapper taskMapper,
                       ValidationService validationService,
                       KafkaTaskProducer kafkaTaskProducer) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.validationService = validationService;

        this.kafkaTaskProducer = kafkaTaskProducer;
    }

    @CustomLoggingStartMethod
    @CustomLoggingFinishedMethod
    @CustomExecutionTime
    @CustomExceptionHandling
    public TaskDto createTask(TaskDto taskDto) {
        if (!validationService.isValidStatus(taskDto.getStatus())) {
            throw new IllegalArgumentException("Invalid status");
        }
        Task taskForDb = taskMapper.toTask(taskDto);
        taskForDb.setId(null);
        Task taskSaveInDb = taskRepository.save(taskForDb);
        return taskMapper.toTaskDto(taskSaveInDb);
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
    public void updateTask(long id, TaskDto taskDto) {
        if (!validationService.isValidStatus(taskDto.getStatus())) {
            throw new IllegalArgumentException("Invalid status");
        }
        Task taskFromDb = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));
        TaskStatus taskFromDbStatus = taskFromDb.getStatus();
        taskFromDb.setTitle(taskDto.getTitle());
        taskFromDb.setDescription(taskDto.getDescription());
        taskFromDb.setUserId(taskDto.getUserId());
        taskFromDb.setStatus(taskDto.getStatus());
        taskRepository.save(taskFromDb);
        if (!taskFromDbStatus.equals(taskDto.getStatus())) {
            TaskUpdateStatusDto taskUpdateStatusDto = new TaskUpdateStatusDto();
            taskUpdateStatusDto.setId(taskFromDb.getId());
            taskUpdateStatusDto.setStatus(taskDto.getStatus());

            kafkaTaskProducer.sendTo(topic, taskUpdateStatusDto);
        }
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
