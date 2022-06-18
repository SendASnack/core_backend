package pt.ua.deti.tqs.sendasnack.core.backend.model.webhooks;

import lombok.*;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.util.Objects;

@Generated
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Hook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NonNull
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "method")
    private HttpMethod method;

    private String body;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hook hook = (Hook) o;
        return Objects.equals(id, hook.id) && url.equals(hook.url) && method == hook.method && Objects.equals(body, hook.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, method, body);
    }

}
