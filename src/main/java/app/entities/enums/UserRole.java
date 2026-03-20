package app.entities.enums;

import io.javalin.security.RouteRole;

public enum UserRole implements RouteRole
{
    TECHNICIAN,
    MANAGER,
    ADMIN
}
