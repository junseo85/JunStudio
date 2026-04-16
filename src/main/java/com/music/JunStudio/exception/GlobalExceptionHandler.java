package com.music.JunStudio.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // This method intercepts ANY DataIntegrityViolationException thrown by the app
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDatabaseConstraints(DataIntegrityViolationException ex) {

        // 1. Get the underlying error message
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : "";

        // 2. Check if it's OUR specific double-booking constraint that tripped
        if (errorMessage.contains("uk_student_lesson_datetime")) {
            // Redirect safely back to the dashboard with a friendly error flag
            return "redirect:/dashboard?error=alreadyAssigned";
        }

        // 3. Fallback for other database constraint issues (like duplicate emails)
        return "redirect:/dashboard?error=dbError";
    }
}