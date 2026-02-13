package app.exceptions.enums;


public enum DatabaseErrorType
{
    CONSTRAINT_VIOLATION , // 409
    NOT_FOUND, // 404
    CONNECTION_FAILURE, // 503
    TRANSACTION_FAILURE, // 500
    QUERY_FAILURE, // 500
    UNKNOWN
}