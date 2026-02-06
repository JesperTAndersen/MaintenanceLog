package app.entities;

import app.utils.UserRole;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

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
    int id;
    String name;
    String phone;
    String email;
    String address;
    UserRole status;
    LocalDate dateOfBirth;
    LocalDate dateOfEnrollment;
    Set<Integer> courseIds; // temporary “relation” by IDs
}

