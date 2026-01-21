package com.cnmci.stats.repository;

import com.cnmci.core.model.PaymentRequest;
import org.springframework.data.repository.CrudRepository;

public interface PaymentRequestRepository extends CrudRepository<PaymentRequest, Long> {
    PaymentRequest findByRequesterTypeAndRequesterIdAndEtat(String type, Long id, int etat);
}
