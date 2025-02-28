package com.t1.task.dto;

import com.t1.task.enums.TaskStatus;

public class TaskUpdateStatusDto {
    private Long id;
    private TaskStatus status;

    public TaskUpdateStatusDto(Long id, TaskStatus status) {
        this.id = id;
        this.status = status;
    }

    public TaskUpdateStatusDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
