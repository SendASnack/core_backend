package pt.ua.deti.tqs.sendasnack.core.backend.model.webhooks;

import lombok.*;
import org.springframework.lang.NonNull;
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
public class WebHook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NonNull
    private String businessUsername;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "hook_id")
    private Hook hook;

    @Enumerated(EnumType.STRING)
    @Column(name = "web_hook_event", nullable = false)
    private WebHookEvent when;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebHook webHook = (WebHook) o;
        return Objects.equals(id, webHook.id) && businessUsername.equals(webHook.businessUsername) && Objects.equals(hook, webHook.hook) && when == webHook.when;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, businessUsername, hook, when);
    }

}
