package pt.ua.deti.tqs.sendasnack.core.backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.lang.NonNull;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.OrderStatus;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Generated
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NonNull
    private String businessUsername;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "costumer_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Costumer costumer;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Order order;

    @NonNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deliveryTime;

    private OrderStatus orderStatus;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderRequest that = (OrderRequest) o;
        return Objects.equals(id, that.id) && businessUsername.equals(that.businessUsername) && Objects.equals(costumer, that.costumer) && Objects.equals(order, that.order) && orderStatus == that.orderStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, businessUsername, costumer, order, deliveryTime, orderStatus);
    }

}
