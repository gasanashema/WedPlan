package rw.ac.auca.wedplan.model;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @NotBlank(message = "User name is required.")
    @Size(min = 2, max = 50, message = "User name must be between 2 and 50 characters.")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Email address is required.")
    @Email(message = "Please enter a valid email address.")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotNull(message = "User role is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @NotNull(message = "User side affiliation is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    private Side side;

    public User() {
    }

    public User(String name, String email, Role role, Side side) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.side = side;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
