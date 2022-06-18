package pt.ua.deti.tqs.sendasnack.core.backend.model.users;

import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.AccountRoleEnum;

import javax.persistence.Entity;
import java.util.Objects;

@Generated
@Entity
@Getter
@Setter
@NoArgsConstructor
public class BusinessUser extends User {

    @NonNull
    private AccountRoleEnum accountType = AccountRoleEnum.BUSINESS;

    public BusinessUser(@lombok.NonNull String username, @lombok.NonNull String email, @lombok.NonNull String password, @lombok.NonNull String name, @lombok.NonNull String phoneNumber) {
        super(username, email, password, name, phoneNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BusinessUser that = (BusinessUser) o;
        return accountType == that.accountType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), accountType);
    }

}
