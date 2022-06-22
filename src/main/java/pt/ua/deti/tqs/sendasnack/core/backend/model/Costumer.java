package pt.ua.deti.tqs.sendasnack.core.backend.model;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Generated
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class Costumer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;
    private String email;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Costumer costumer = (Costumer) o;
        return Objects.equals(id, costumer.id) && Objects.equals(name, costumer.name) && Objects.equals(email, costumer.email) && Objects.equals(address, costumer.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, address);
    }

}
