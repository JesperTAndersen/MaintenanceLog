package app.entities;

import app.utils.UserRole;
import jakarta.persistence.*;
import lombok.*;


@Getter
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@Entity
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    int userId;

    @Setter
    @Column(name = "first_name", nullable = false)
    String firstName;

    @Setter
    @Column(name = "last_name", nullable = false)
    String lastName;

    @Setter
    @Column(name = "phone", nullable = false)
    String phone;

    @Setter
    @Column(name = "email", nullable = false, unique = true)
    String email;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    UserRole role;

    @Setter
    @Column(name = "active", nullable = false)
    boolean active;
}