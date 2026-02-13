package app.exceptions.enums;


public enum DatabaseErrorType
{
    UNIQUE_VIOLATION,
    FOREIGN_KEY_VIOLATION,
    NOT_FOUND,
    NOT_NULL_VIOLATION,
    CONNECTION_FAILURE,
    TRANSACTION_FAILURE,
    UNKNOWN
}