package com.cnmci.stats.repository;

import com.cnmci.core.model.PaymentRequestCopie;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRequestCopieRepository extends CrudRepository<PaymentRequestCopie, Long> {
}
