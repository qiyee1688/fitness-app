package com.fitness.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_REQUEST(400, "Invalid request", HttpStatus.BAD_REQUEST),
    EXERCISE_NOT_FOUND(40401, "Exercise not found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(40402, "User not found", HttpStatus.NOT_FOUND),
    USER_PROFILE_NOT_FOUND(40403, "User profile not found", HttpStatus.NOT_FOUND),
    ACTIVE_PLAN_NOT_FOUND(40404, "Active plan not found", HttpStatus.NOT_FOUND),
    TODAY_WORKOUT_NOT_FOUND(40405, "No workout is scheduled for this date", HttpStatus.NOT_FOUND),
    WORKOUT_NOT_FOUND(40406, "Workout not found in active plan", HttpStatus.NOT_FOUND),
    PRESCRIPTION_NOT_FOUND(40407, "Exercise is not available in this workout", HttpStatus.NOT_FOUND),
    PLAN_GENERATION_FAILED(42201, "No exercises available for plan generation", HttpStatus.UNPROCESSABLE_ENTITY),
    PLAN_CONFLICT(40901, "Plan was changed concurrently; please retry", HttpStatus.CONFLICT),
    FEEDBACK_CONFLICT(40902, "Workout changed while applying feedback; please retry", HttpStatus.CONFLICT),
    INTERNAL_ERROR(500, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
