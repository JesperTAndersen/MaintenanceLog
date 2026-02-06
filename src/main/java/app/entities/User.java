package app.entities;

import app.utils.UserRole;
import jakarta.persistence.*;
import lombok.*;


@Getter
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Entity
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    int userId;

    @Column(name = "first_name", nullable = false)
    String firstName;

    @Column(name = "last_name", nullable = false)
    String lastName;

    @Column(name = "phone", nullable = false)
    String phone;

    @Column(name = "email", nullable = false)
    String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    UserRole role;
}

