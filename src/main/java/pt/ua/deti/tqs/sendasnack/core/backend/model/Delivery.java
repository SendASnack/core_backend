package pt.ua.deti.tqs.sendasnack.core.backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.lang.NonNull;
import pt.ua.deti.tqs.sendasnack.core.backend.model.users.RiderUser;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.DeliveryStatus;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Generated
@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
    @JoinColumn(name = "order_request_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private OrderRequest orderRequest;

    @NonNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deliveryPrediction;

    private DeliveryStatus deliveryStatus;

    @ManyToOne
    @JoinColumn(name = "accepted_by_rider_id")
    private RiderUser rider;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Delivery delivery = (Delivery) o;
        return Objects.equals(id, delivery.id) && Objects.equals(orderRequest, delivery.orderRequest) && deliveryPrediction.equals(delivery.deliveryPrediction) && Objects.equals(rider, delivery.rider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderRequest, deliveryPrediction, rider);
    }

}
