package app.services;

import app.dtos.CreateEmployeeRequest;
import app.dtos.EmployeeDTO;
import app.dtos.EmployeeLoginDTO;
import app.entities.Employee;
import app.exceptions.ApiException;
import app.exceptions.ValidationException;
import app.mappers.EmployeeMapper;
import app.persistence.interfaces.ISecurityDAO;
import app.services.interfaces.SecurityService;
import app.utils.PropertyReader;
import dk.bugelhartmann.ITokenSecurity;
import dk.bugelhartmann.TokenSecurity;
import dk.bugelhartmann.TokenVerificationException;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.HttpStatus;
import io.javalin.http.UnauthorizedResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.text.ParseException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityServiceImpl implements SecurityService
{
    private final ISecurityDAO secDAO;
    private final ITokenSecurity tokenSecurity = new TokenSecurity();
    private static final Map<String, Set<String>> ROLE_HIERARCHY = Map.of(
            "ADMIN", Set.of("ADMIN", "MANAGER", "TECHNICIAN", "AUTHENTICATED"),
            "MANAGER", Set.of("MANAGER", "TECHNICIAN","AUTHENTICATED"),
            "TECHNICIAN", Set.of("TECHNICIAN","AUTHENTICATED"));

    public SecurityServiceImpl(ISecurityDAO secDAO)
    {
        this.secDAO = secDAO;
    }

    public static String hashPassword(String password)
    {
        String salt = BCrypt.gensalt(12);
        return BCrypt.hashpw(password, salt);
    }

    public static boolean verifyPassword(String inputtedPassword, String hashedPassword)
    {
        return BCrypt.checkpw(inputtedPassword, hashedPassword);
    }

    @Override
    public EmployeeDTO register(CreateEmployeeRequest request)
    {
        if (secDAO.getByEmail(request.email()) != null)
        {
            throw new ApiException(409, "Email already exists");
        }

        //TODO: validate inputs. implement validator util class
        Employee employee = Employee.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .role(request.role())
                .password(hashPassword(request.password()))
                .active(true)
                .build();

        Employee created = secDAO.create(employee);
        return EmployeeMapper.toDTO(created);

    }

    @Override
    public Map<String, Object> login(EmployeeLoginDTO dto)
    {
        try
        {
            Employee verifiedEmployee = secDAO.getVerifiedEmployee(dto.email(), dto.password());

            EmployeeDTO employeeDTO = EmployeeMapper.toDTO(verifiedEmployee);
            if (!employeeDTO.active())
            {
                throw new ApiException(403, "Permission Denied");
            }

            String token = createToken(employeeDTO);

            return Map.of(
                    "token", token,
                    "employee", employeeDTO
            );

        }
        catch (ValidationException e)
        {
            throw new ApiException(401, e.getMessage());
        }
    }

    @Override
    public void authenticate(Context ctx)
    {
        if (ctx.method().toString().equals("OPTIONS")) // This is a preflight request => no need for authentication
        {
            ctx.status(200);
            return;
        }

        // If the endpoint is not protected with roles, then skip
        Set<String> allowedRoles = ctx.routeRoles().stream()
                .map(role -> role.toString().toUpperCase())
                .collect(Collectors.toSet());

        if (isOpenEndpoint(allowedRoles))
            return;

        // If there is no token we do not allow entry
        UserDTO verifiedTokenEmployee = validateAndGetEmployeeFromToken(ctx);
        ctx.attribute("employee", verifiedTokenEmployee);
    }

    @Override
    public void authorize(Context ctx)
    {
        Set<String> allowedRoles = ctx.routeRoles()
                .stream()
                .map(role -> role.toString().toUpperCase())
                .collect(Collectors.toSet());

        // 1. Check if the endpoint is open to all (either by not having any roles or having the ANYONE role set
        if (isOpenEndpoint(allowedRoles))
            return;

        // 2. Get employee and ensure it is not null
        UserDTO employee = ctx.attribute("employee");
        if (employee == null)
        {
            throw new ForbiddenResponse("No employee was added from the token");
        }

        // 3. See if any role matches
        if (!employeeHasAllowedRole(employee, allowedRoles))
        {
            throw new ForbiddenResponse("Employee was not authorized with roles: " + employee.getRoles() + ". Needed roles are: " + allowedRoles);
        }
    }


    private String createToken(EmployeeDTO employeeDTO)
    {
        try
        {
            UserDTO libraryDTO = convertToLibraryDTO(employeeDTO);
            String ISSUER;
            String TOKEN_EXPIRE_TIME;
            String SECRET_KEY;

            if (System.getenv("DEPLOYED") != null)
            {
                ISSUER = System.getenv("ISSUER");
                TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
                SECRET_KEY = System.getenv("SECRET_KEY");
            }
            else
            {
                ISSUER = PropertyReader.getPropertyValue("ISSUER", "config.properties");
                TOKEN_EXPIRE_TIME = PropertyReader.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties");
                SECRET_KEY = PropertyReader.getPropertyValue("SECRET_KEY", "config.properties");
            }

            return tokenSecurity.createToken(libraryDTO, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
        }
        catch (Exception e)
        {
            //            logger.error("Could not create token", e);
            throw new ApiException(500, "Could not create token");
        }
    }

    private UserDTO convertToLibraryDTO(EmployeeDTO employeeDTO)
    {
        return new UserDTO(
                employeeDTO.email(),
                Set.of(employeeDTO.role().name())
        );
    }

    private static String getToken(Context ctx)
    {
        String header = ctx.header("Authorization");
        if (header == null)
        {
            throw new UnauthorizedResponse("Authorization header is missing");
        }

        // If the Authorization Header was malformed, then no entry
        String token = header.split(" ")[1];
        if (token == null)
        {
            throw new UnauthorizedResponse("Authorization header is malformed");
        }
        return token;
    }

    private boolean isOpenEndpoint(Set<String> allowedRoles)
    {
        // If the endpoint is not protected with any roles:
        if (allowedRoles.isEmpty())
            return true;

        // 1. Get permitted roles and Check if the endpoint is open to all with the ANYONE role
        if (allowedRoles.contains("ANYONE"))
        {
            return true;
        }
        return false;
    }

    private UserDTO validateAndGetEmployeeFromToken(Context ctx)
    {
        String token = getToken(ctx);
        UserDTO verifiedTokenEmployee = verifyToken(token);
        if (verifiedTokenEmployee == null)
        {
            throw new UnauthorizedResponse("Invalid employee or token");
        }
        return verifiedTokenEmployee;
    }

    private UserDTO verifyToken(String token)
    {
        String SECRET = PropertyReader.getPropertyValue("SECRET_KEY", "config.properties");

        try
        {
            if (tokenSecurity.tokenIsValid(token, SECRET) && tokenSecurity.tokenNotExpired(token))
            {
                return tokenSecurity.getUserWithRolesFromToken(token);
            }
            else
            {
                throw new ApiException(403, "Token is not valid");
            }
        }
        catch (ParseException | TokenVerificationException e)
        {
            throw new ApiException(HttpStatus.UNAUTHORIZED.getCode(), "Unauthorized. Could not verify token");
        }
    }

    private static boolean employeeHasAllowedRole(UserDTO employee, Set<String> allowedRoles)
    {
        Set<String> employeeRoles = employee.getRoles();

        if (employeeRoles.isEmpty())
        {
            return false;
        }

        String employeeRole = employeeRoles.iterator().next();

        Set<String> effectiveRoles = ROLE_HIERARCHY.getOrDefault(employeeRole, Set.of(employeeRole));

        return effectiveRoles.stream()
                .anyMatch(role -> allowedRoles.contains(role.toUpperCase()));
    }
}
