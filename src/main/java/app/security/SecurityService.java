package app.security;

import app.dtos.CreateEmployeeRequest;
import app.dtos.EmployeeDTO;
import io.javalin.http.Context;

import java.util.Map;

public interface SecurityService
{
    void authenticate(Context ctx);

    void authorize(Context ctx);

    EmployeeDTO register(CreateEmployeeRequest request);

    Map<String, Object> login(EmployeeLoginDTO dto);

}