package com.t1.task.service;

import com.t1.task.aspect.annotation.CustomLoggingFinishedMethod;
import com.t1.task.aspect.annotation.CustomLoggingStartMethod;
import com.t1.task.enums.TaskStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class ValidationService {
    @CustomLoggingStartMethod
    @CustomLoggingFinishedMethod
    public boolean isValidStatus(TaskStatus status) {
        return Arrays.asList(TaskStatus.values()).contains(status);
    }
}
