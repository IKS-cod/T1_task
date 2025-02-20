package com.t1.task.mapper;

import com.t1.task.dto.TaskDto;
import com.t1.task.dto.TaskWithoutIdDto;
import com.t1.task.model.Task;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    TaskDto toTaskDto(Task task);
    Task toTask(TaskDto taskDto);
    TaskWithoutIdDto toTaskWithoutIdDto(Task task);
    Task toTask(TaskWithoutIdDto taskWithoutIdDto);



}
