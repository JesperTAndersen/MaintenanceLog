package app.security;

import app.dtos.CreateUserRequest;
import app.dtos.UserDTO;
import app.exceptions.ValidationException;
import io.javalin.http.Context;

import java.util.Map;

public interface SecurityService
{
    void authenticate(Context ctx);

    void authorize(Context ctx);

    UserDTO register(CreateUserRequest request);

    Map<String, Object> login(UserLoginDTO dto);

}