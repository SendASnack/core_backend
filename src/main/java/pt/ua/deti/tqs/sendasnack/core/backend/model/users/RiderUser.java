package pt.ua.deti.tqs.sendasnack.core.backend.model.users;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.lang.NonNull;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.AccountRoleEnum;
import pt.ua.deti.tqs.sendasnack.core.backend.model.Delivery;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.AvailabilityStatus;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Generated
@Entity
@Getter
@Setter
@NoArgsConstructor
public class RiderUser extends User {

    @NonNull
    private AccountRoleEnum accountType = AccountRoleEnum.RIDER;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "rider_rejected_deliveries",
            joinColumns = @JoinColumn(name = "rider_user_id"),
            inverseJoinColumns = @JoinColumn(name = "deliveries_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Delivery> rejectedDeliveries = new LinkedHashSet<>();

    @OneToMany(mappedBy = "rider", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Delivery> acceptedDeliveries = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false)
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.OFFLINE;

    public RiderUser(@lombok.NonNull String username, @lombok.NonNull String email, @lombok.NonNull String password, @lombok.NonNull String name, @lombok.NonNull String phoneNumber, Set<Delivery> rejectedDeliveries, Set<Delivery> acceptedDeliveries) {
        super(username, email, password, name, phoneNumber);
        this.rejectedDeliveries = rejectedDeliveries;
        this.acceptedDeliveries = acceptedDeliveries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RiderUser riderUser = (RiderUser) o;
        return accountType == riderUser.accountType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), accountType);
    }

}
