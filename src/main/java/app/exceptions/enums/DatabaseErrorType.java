package app.exceptions.enums;


public enum DatabaseErrorType
{
    CONSTRAINT_VIOLATION , // 409 - Conflict with current state (e.g., duplicate/wrong version).
    NOT_FOUND, // 404 - Resource does not exist, wrong URL.
    CONNECTION_FAILURE, // 503 - Server overloaded or under maintenance.
    TRANSACTION_FAILURE, // 500 - Unexpected server error.
    QUERY_FAILURE, // 500
    UNKNOWN
}