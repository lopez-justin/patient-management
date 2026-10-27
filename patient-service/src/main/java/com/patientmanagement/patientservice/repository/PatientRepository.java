package com.patientmanagement.patientservice.repository;

import com.patientmanagement.patientservice.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<PatientEntity, String> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, String id);

}
