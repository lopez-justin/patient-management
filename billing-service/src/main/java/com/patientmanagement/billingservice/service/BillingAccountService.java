package com.patientmanagement.billingservice.service;

import com.patientmanagement.billingservice.entity.BillingAccountEntity;
import com.patientmanagement.billingservice.entity.BillingAccountStatus;
import com.patientmanagement.billingservice.repository.BillingAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Service
public class BillingAccountService {

    private final BillingAccountRepository repository;

    public BillingAccountService(BillingAccountRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BillingAccountEntity create(String patientId, String patientName, String patientEmail) {
        String normalizedPatientId = requireText(patientId, "patient_id");
        String normalizedName = requireText(patientName, "patient_name");
        String normalizedEmail = requireText(patientEmail, "patient_email").toLowerCase(Locale.ROOT);

        return repository.findByPatientId(normalizedPatientId).orElseGet(() -> {
            BillingAccountEntity account = new BillingAccountEntity();
            account.setPatientId(normalizedPatientId);
            account.setPatientName(normalizedName);
            account.setPatientEmail(normalizedEmail);
            account.setStatus(BillingAccountStatus.ACTIVE);
            return repository.saveAndFlush(account);
        });
    }

    private String requireText(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
