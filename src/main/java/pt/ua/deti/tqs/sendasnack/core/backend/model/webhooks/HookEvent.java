package pt.ua.deti.tqs.sendasnack.core.backend.model.webhooks;

import lombok.*;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.WebHookEvent;

import javax.persistence.*;
import java.util.Objects;

@Generated
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class HookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "property")
    private WebHookEvent property;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HookEvent hookEvent = (HookEvent) o;
        return Objects.equals(id, hookEvent.id) && property == hookEvent.property;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, property);
    }

}
