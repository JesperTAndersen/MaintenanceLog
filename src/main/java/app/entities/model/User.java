package app.entities.model;

import app.entities.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;


@Getter
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
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
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Setter
    @Column(name = "active", nullable = false)
    private boolean active;

    public User(String firstName, String lastName, String phone, String email, UserRole role, boolean active)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.active = active;
    }
}