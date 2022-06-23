package pt.ua.deti.tqs.sendasnack.core.backend.repository;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ua.deti.tqs.sendasnack.core.backend.model.Delivery;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    List<Delivery> findAllByRiderUsernameIsNull();

    @NonNull Optional<Delivery> findById(@NonNull Long id);

}
