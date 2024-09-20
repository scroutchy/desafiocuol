package br.com.desafio.repository;

import br.com.desafio.domain.model.PaymentModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<PaymentModel, String> {
    Optional<PaymentModel> findByClientId(String clientId);
}
