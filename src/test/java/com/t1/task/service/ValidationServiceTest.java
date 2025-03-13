package com.t1.task.service;

import com.t1.task.enums.TaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {
    @Spy
    private ValidationService validationService;

    @Test
    void isValidStatusValidStatusReturnsTrueTest() {
        assertTrue(validationService.isValidStatus(TaskStatus.IN_PROGRESS));
        assertTrue(validationService.isValidStatus(TaskStatus.NEW));
        assertTrue(validationService.isValidStatus(TaskStatus.COMPLETED));
    }

    @Test
    public void isValidStatusInvalidStatusReturnsFalseTest() {
        assertFalse(validationService.isValidStatus(null));
    }
}