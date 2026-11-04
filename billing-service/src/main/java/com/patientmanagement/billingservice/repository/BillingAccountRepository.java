package com.patientmanagement.billingservice.repository;

import com.patientmanagement.billingservice.entity.BillingAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillingAccountRepository extends JpaRepository<BillingAccountEntity, String> {

    Optional<BillingAccountEntity> findByPatientId(String patientId);
}
