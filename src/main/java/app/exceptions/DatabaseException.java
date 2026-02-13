package app.exceptions;

import app.exceptions.enums.DatabaseErrorType;

public class DatabaseException extends RuntimeException {
    private final DatabaseErrorType errorType;

    public DatabaseException(String message, DatabaseErrorType  errorType) {
        super(message);
        this.errorType = errorType;
    }

    public DatabaseException(String message, DatabaseErrorType  errorType, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public DatabaseErrorType getErrorType() {
        return errorType;
    }
}