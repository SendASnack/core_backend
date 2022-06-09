package pt.ua.deti.tqs.sendasnack.core.backend.model;

import lombok.*;
import org.hibernate.Hibernate;
import pt.ua.deti.tqs.sendasnack.core.backend.dao.AccountRoleEnum;

import javax.persistence.*;
import java.util.Objects;

@Generated
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @NonNull
    private String username;

    @NonNull
    private String email;

    @NonNull
    private String password;

    @NonNull
    private String name;

    @NonNull
    private String phoneNumber;

    @NonNull
    @Enumerated(EnumType.STRING)
    private AccountRoleEnum accountRoles;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && username.equals(user.username) && email.equals(user.email) && password.equals(user.password) && name.equals(user.name) && phoneNumber.equals(user.phoneNumber) && accountRoles == user.accountRoles;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, password, name, phoneNumber, accountRoles);
    }

}
