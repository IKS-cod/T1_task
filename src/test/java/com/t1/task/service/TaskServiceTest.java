package com.t1.task.service;

import com.t1.task.dto.TaskDto;
import com.t1.task.dto.TaskUpdateStatusDto;
import com.t1.task.enums.TaskStatus;
import com.t1.task.exception.TaskNotFoundException;
import com.t1.task.kafka.KafkaTaskProducer;
import com.t1.task.mapper.TaskMapper;
import com.t1.task.model.Task;
import com.t1.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    private final TaskMapper taskMapper = Mappers.getMapper(TaskMapper.class);
    @Spy
    private ValidationService validationService;
    @Mock
    private KafkaTaskProducer kafkaTaskProducer;

    @Test
    void createTaskValidInputReturnsTaskDtoTest() {
        // Arrange
        TaskDto taskDto = new TaskDto();
        taskDto.setId(10L);
        taskDto.setTitle("Test_task");
        taskDto.setDescription("Test_description");
        taskDto.setUserId(123L);
        taskDto.setStatus(TaskStatus.IN_PROGRESS);

        Task taskSaveInDb = new Task();
        taskSaveInDb.setId(1L);
        taskSaveInDb.setTitle("Test_task");
        taskSaveInDb.setDescription("Test_description");
        taskSaveInDb.setUserId(123L);
        taskSaveInDb.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.save(any(Task.class))).thenReturn(taskSaveInDb);
        TaskService taskService = new TaskService(taskRepository,
                taskMapper, validationService, kafkaTaskProducer);

        // Act
        TaskDto actual = taskService.createTask(taskDto);

        // Assert
        assertNotNull(actual);
        assertEquals(taskSaveInDb.getId(), actual.getId());
        assertEquals(taskDto.getTitle(), actual.getTitle());
        assertEquals(taskDto.getDescription(), actual.getDescription());
        assertEquals(taskDto.getUserId(), actual.getUserId());
        assertEquals(taskDto.getStatus(), actual.getStatus());

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTaskInvalidStatusThrowsExceptionTest() {
        // Arrange
        TaskDto taskDto = new TaskDto();
        taskDto.setStatus(null);
        TaskService taskService = new TaskService(taskRepository, taskMapper, validationService, kafkaTaskProducer);
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> taskService.createTask(taskDto));
    }

    @Test
    void getTaskExistingIdReturnsTaskDtoTest() {
        // Arrange
        long taskId = 1L;
        Task taskFromDb = new Task();
        taskFromDb.setId(taskId);
        taskFromDb.setTitle("Test_task");
        taskFromDb.setDescription("Test_description");
        taskFromDb.setUserId(123L);
        taskFromDb.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskFromDb));
        TaskService taskService = new TaskService(taskRepository, taskMapper, validationService, kafkaTaskProducer);
        // Act
        TaskDto actual = taskService.getTask(taskId);

        // Assert
        assertNotNull(actual);
        assertEquals(taskId, actual.getId());
        assertEquals(taskFromDb.getTitle(), actual.getTitle());
        assertEquals(taskFromDb.getDescription(), actual.getDescription());
        assertEquals(taskFromDb.getUserId(), actual.getUserId());
        assertEquals(taskFromDb.getStatus(), actual.getStatus());

        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void getTaskNonExistingIdThrowsExceptionTest() {
        // Arrange
        long taskId = 1L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        TaskService taskService = new TaskService(taskRepository, taskMapper, validationService, kafkaTaskProducer);
        // Act & Assert
        assertThrows(TaskNotFoundException.class, () -> taskService.getTask(taskId));
    }

    @Test
    void updateTaskExistingIdAndValidStatusUpdatesTaskTest() {
        // Arrange
        long taskId = 1L;
        TaskDto taskDto = new TaskDto();
        taskDto.setTitle("Updated_task");
        taskDto.setDescription("Updated_description");
        taskDto.setUserId(456L);
        taskDto.setStatus(TaskStatus.NEW);

        Task taskFromDb = new Task();
        taskFromDb.setId(taskId);
        taskFromDb.setTitle("Original_task");
        taskFromDb.setDescription("Original_description");
        taskFromDb.setUserId(123L);
        taskFromDb.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskFromDb));
        TaskService taskService = new TaskService(taskRepository, taskMapper, validationService, kafkaTaskProducer);

        // Act
        taskService.updateTask(taskId, taskDto);
        // Assert
        assertEquals(taskDto.getTitle(), taskFromDb.getTitle());
        assertEquals(taskDto.getDescription(), taskFromDb.getDescription());
        assertEquals(taskDto.getUserId(), taskFromDb.getUserId());
        assertEquals(taskDto.getStatus(), taskFromDb.getStatus());
        verify(taskRepository, times(1)).save(taskFromDb);
        verify(kafkaTaskProducer).sendTo(any(), any(TaskUpdateStatusDto.class));
    }


    @Test
    void updateTaskNonExistingIdThrowsExceptionTest() {
        // Arrange
        long taskId = 1L;
        TaskDto taskDto = new TaskDto();
        taskDto.setTitle("Updated Task");
        taskDto.setDescription("Updated Description");
        taskDto.setUserId(456L);
        taskDto.setStatus(TaskStatus.NEW);

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        TaskService taskService = new TaskService(taskRepository, taskMapper, validationService, kafkaTaskProducer);
        // Act & Assert
        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(taskId, taskDto));
    }

    @Test
    void updateTaskInvalidStatusThrowsExceptionTest() {
        // Arrange
        long taskId = 1L;
        TaskDto taskDto = new TaskDto();
        taskDto.setTitle("Updated Task");
        taskDto.setDescription("Updated Description");
        taskDto.setUserId(456L);
        taskDto.setStatus(null);
        TaskService taskService = new TaskService(taskRepository, taskMapper, validationService, kafkaTaskProducer);
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> taskService.updateTask(taskId, taskDto));
    }

    @Test
    void deleteTaskExistingIdDeletesTaskTest() {
        // Arrange
        long taskId = 1L;
        when(taskRepository.existsById(taskId)).thenReturn(true);
        TaskService taskService = new TaskService(taskRepository, taskMapper, validationService, kafkaTaskProducer);
        // Act
        taskService.deleteTask(taskId);

        // Assert
        verify(taskRepository, times(1)).deleteById(taskId);
    }

    @Test
    void deleteTaskNonExistingIdThrowsExceptionTest() {
        // Arrange
        long taskId = 1L;
        when(taskRepository.existsById(taskId)).thenReturn(false);
        TaskService taskService = new TaskService(taskRepository, taskMapper, validationService, kafkaTaskProducer);
        // Act & Assert
        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(taskId));
    }

    @Test
    void findAllTaskReturnsListOfTaskDtoTest() {
        // Arrange
        List<Task> taskListFromDb = new ArrayList<>();
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task_1");
        task1.setDescription("Description_1");
        task1.setUserId(123L);
        task1.setStatus(TaskStatus.IN_PROGRESS);
        taskListFromDb.add(task1);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task_2");
        task2.setDescription("Description_2");
        task2.setUserId(456L);
        task2.setStatus(TaskStatus.NEW);
        taskListFromDb.add(task2);

        when(taskRepository.findAll()).thenReturn(taskListFromDb);
        TaskService taskService = new TaskService(taskRepository, taskMapper, validationService, kafkaTaskProducer);
        // Act
        Collection<TaskDto> actual = taskService.findAllTask();

        // Assert
        assertNotNull(actual);
        assertEquals(2, actual.size());

        List<TaskDto> actualResultList = new ArrayList<>(actual);

        assertEquals(task1.getId(), actualResultList.get(0).getId());
        assertEquals(task1.getTitle(), actualResultList.get(0).getTitle());
        assertEquals(task1.getDescription(), actualResultList.get(0).getDescription());
        assertEquals(task1.getUserId(), actualResultList.get(0).getUserId());
        assertEquals(task1.getStatus(), actualResultList.get(0).getStatus());

        assertEquals(task2.getId(), actualResultList.get(1).getId());
        assertEquals(task2.getTitle(), actualResultList.get(1).getTitle());
        assertEquals(task2.getDescription(), actualResultList.get(1).getDescription());
        assertEquals(task2.getUserId(), actualResultList.get(1).getUserId());
        assertEquals(task2.getStatus(), actualResultList.get(1).getStatus());

        verify(taskRepository, times(1)).findAll();
    }
}
