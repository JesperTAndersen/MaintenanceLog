package app.entities.enums;

import io.javalin.security.RouteRole;

public enum EmployeeRole implements RouteRole
{
    TECHNICIAN,
    MANAGER,
    ADMIN,
    AUTHENTICATED
}
