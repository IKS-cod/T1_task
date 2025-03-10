package com.t1.task.model;

import com.t1.task.enums.TaskStatus;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;
    private String description;
    private Long userId;
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    public Task(Long id, String title, String description, Long userId, TaskStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.userId = userId;
        this.status = status;
    }

    public Task() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id) && Objects.equals(title, task.title) && Objects.equals(description, task.description) && Objects.equals(userId, task.userId) && Objects.equals(status, task.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, userId, status);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", userId=" + userId +
                ", status=" + status +
                '}';
    }
}
