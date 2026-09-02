package com.epicode.buildweekbackend3.exceptions;

import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.stream.Collectors;

public class ValidationException extends RuntimeException {

    private List<ObjectError> errorsList;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(List<ObjectError> errorsList) {
        super(errorsList.stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining(", ")));
        this.errorsList = errorsList;
    }

    public List<ObjectError> getErrorsList() {
        return errorsList;
    }
}