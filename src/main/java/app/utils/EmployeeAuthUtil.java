package app.utils;

import app.exceptions.ApiException;
import app.services.interfaces.EmployeeIdentityService;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.Context;

public final class EmployeeAuthUtil
{
    private EmployeeAuthUtil()
    {
    }

    public record AuthenticatedEmployee(UserDTO user, Integer id) {}

    public static AuthenticatedEmployee requireAuthenticatedEmployee(Context ctx, EmployeeIdentityService employeeIdentityService)
    {
        UserDTO tokenUser = ctx.attribute("authUser");
        if (tokenUser == null)
        {
            throw new ApiException(401, "Missing authenticated Employee");
        }

        String authEmail = tokenUser.getUsername();
        Integer performedById = employeeIdentityService.getEmployeeIdByEmail(authEmail);

        if (performedById == null)
        {
            throw new ApiException(401, "Missing authenticated Employee ID");
        }

        return new AuthenticatedEmployee(tokenUser, performedById);
    }
}
