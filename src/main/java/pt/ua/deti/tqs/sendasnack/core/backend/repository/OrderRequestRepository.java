package pt.ua.deti.tqs.sendasnack.core.backend.repository;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ua.deti.tqs.sendasnack.core.backend.model.OrderRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRequestRepository extends JpaRepository<OrderRequest, Long> {

    @NonNull Optional<OrderRequest> findById(@NonNull Long id);

    List<OrderRequest> findAllByBusinessUsername(String businessUserName);

}
