package facu.studer.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * Entity representing a User.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class User extends BaseEntity  {

    /**
     * Unique username.
     */
    @Column(nullable = false)
    private String firstName;

    /**
     * Unique username.
     */
    @Column(nullable = false)
    private String lastName;

    /**
     * Unique username.
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     *  birthdate.
     */
    @Column(nullable = false)
    private LocalDate birthDate;

    /**
     * Registered email.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Hashed password.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Last connection time.
     */
    @Column()
    private LocalDateTime lastConnectionTime;


}
