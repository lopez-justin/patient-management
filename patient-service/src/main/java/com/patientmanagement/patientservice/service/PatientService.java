package com.patientmanagement.patientservice.service;

import com.patientmanagement.patientservice.client.BillingAccountClient;
import com.patientmanagement.patientservice.dto.PatientRequest;
import com.patientmanagement.patientservice.dto.PatientResponse;
import com.patientmanagement.patientservice.entity.PatientEntity;
import com.patientmanagement.patientservice.exception.DuplicateEmailException;
import com.patientmanagement.patientservice.exception.PatientNotFoundException;
import com.patientmanagement.patientservice.mapper.PatientMapper;
import com.patientmanagement.patientservice.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final BillingAccountClient billingAccountClient;

    public PatientService(
            PatientRepository patientRepository,
            PatientMapper patientMapper,
            BillingAccountClient billingAccountClient
    ) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
        this.billingAccountClient = billingAccountClient;
    }

    @Transactional
    public PatientResponse create(PatientRequest request) {
        String email = normalizeEmail(request.email());
        if (patientRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        PatientEntity patient = patientMapper.toEntity(request);
        patient.setEmail(email);
        PatientEntity savedPatient = patientRepository.saveAndFlush(patient);
        billingAccountClient.createBillingAccount(
                savedPatient.getId(),
                savedPatient.getName(),
                savedPatient.getEmail());
        return patientMapper.toResponse(savedPatient);
    }

    @Transactional(readOnly = true)
    public PatientResponse findById(String id) {
        return patientMapper.toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> findAll(Pageable pageable) {
        return patientRepository.findAll(pageable).map(patientMapper::toResponse);
    }

    @Transactional
    public PatientResponse update(String id, PatientRequest request) {
        PatientEntity patient = findEntity(id);

        String email = normalizeEmail(request.email());
        if (patientRepository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateEmailException(email);
        }

        patient.setName(request.name());
        patient.setEmail(email);
        patient.setAddress(request.address());
        patient.setBirthDate(request.birthDate());
        patient.setPhoneNumber(request.phoneNumber());
        return patientMapper.toResponse(patientRepository.saveAndFlush(patient));
    }

    @Transactional
    public void delete(String id) {
        patientRepository.delete(findEntity(id));
    }

    private PatientEntity findEntity(String id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

}
