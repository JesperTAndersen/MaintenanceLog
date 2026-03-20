package app.entities;

import app.dtos.EmployeeDTO;
import app.entities.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@Entity
@Table(name = "employees")
public class Employee
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id", nullable = false)
    private Integer userId;
    @Setter
    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Setter
    @Column(name = "last_name", nullable = false)
    private String lastName;
    @Setter
    @Column(name = "phone", nullable = false)
    private String phone;
    @Setter
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Setter
    @Column(name = "password", nullable = false)
    private String password;
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;
    @Setter
    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Employee(String firstName, String lastName, String phone, String email, UserRole role, boolean active)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.active = active;
        this.password = "default";
    }

    public Employee(String firstName, String lastName, String phone, String email, String password, UserRole role, boolean active)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = active;
    }

    public Employee(EmployeeDTO employeeDTO, String password)
    {
        this.firstName = employeeDTO.firstName();
        this.lastName = employeeDTO.lastName();
        this.phone = employeeDTO.phone();
        this.email = employeeDTO.email();
        this.role = employeeDTO.role();
        this.password = password;
    }
}