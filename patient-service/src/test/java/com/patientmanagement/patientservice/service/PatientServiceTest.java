package com.patientmanagement.patientservice.service;

import com.patientmanagement.patientservice.client.BillingAccountClient;
import com.patientmanagement.patientservice.dto.PatientRequest;
import com.patientmanagement.patientservice.dto.PatientResponse;
import com.patientmanagement.patientservice.entity.PatientEntity;
import com.patientmanagement.patientservice.exception.BillingServiceUnavailableException;
import com.patientmanagement.patientservice.exception.DuplicateEmailException;
import com.patientmanagement.patientservice.exception.PatientNotFoundException;
import com.patientmanagement.patientservice.mapper.PatientMapper;
import com.patientmanagement.patientservice.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    private static final String TEST_EMAIL = "justinlopez@example.com";

    private static final PatientRequest REQUEST = new PatientRequest(
            "Justin Lopez",
            " JustinLopez@Example.COM ",
            "123 Main St",
            LocalDate.of(1990, 1, 15),
            "123456789"
    );

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private BillingAccountClient billingAccountClient;

    @InjectMocks
    private PatientService patientService;

    @Test
    void createNormalizesEmailAndReturnsResponse() {
        PatientEntity entity = new PatientEntity();
        entity.setId("patient-id");
        entity.setName("Justin Lopez");
        PatientResponse response = response(TEST_EMAIL);

        when(patientRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(patientMapper.toEntity(REQUEST)).thenReturn(entity);
        when(patientRepository.saveAndFlush(entity)).thenReturn(entity);
        when(patientMapper.toResponse(entity)).thenReturn(response);

        PatientResponse result = patientService.create(REQUEST);

        assertThat(result).isEqualTo(response);
        assertThat(entity.getEmail()).isEqualTo(TEST_EMAIL);
        verify(patientRepository).saveAndFlush(entity);
        verify(billingAccountClient).createBillingAccount("patient-id", "Justin Lopez", TEST_EMAIL);
    }

    @Test
    void createThrowsWhenBillingServiceFails() {
        PatientEntity entity = new PatientEntity();
        entity.setId("patient-id");
        entity.setName("Justin Lopez");

        when(patientRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(patientMapper.toEntity(REQUEST)).thenReturn(entity);
        when(patientRepository.saveAndFlush(entity)).thenReturn(entity);
        doThrow(new BillingServiceUnavailableException(new RuntimeException("down")))
                .when(billingAccountClient)
                .createBillingAccount(anyString(), anyString(), anyString());

        assertThatThrownBy(() -> patientService.create(REQUEST))
                .isInstanceOf(BillingServiceUnavailableException.class);
    }

    @Test
    void createThrowsWhenEmailAlreadyExists() {
        when(patientRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> patientService.create(REQUEST))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining(TEST_EMAIL);

        verify(patientRepository, never()).saveAndFlush(any());
    }

    @Test
    void findByIdReturnsPatient() {
        PatientEntity entity = new PatientEntity();
        PatientResponse response = response(TEST_EMAIL);

        when(patientRepository.findById("patient-id")).thenReturn(Optional.of(entity));
        when(patientMapper.toResponse(entity)).thenReturn(response);

        assertThat(patientService.findById("patient-id")).isEqualTo(response);
    }

    @Test
    void findByIdThrowsWhenPatientDoesNotExist() {
        when(patientRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.findById("missing-id"))
                .isInstanceOf(PatientNotFoundException.class)
                .hasMessageContaining("missing-id");
    }

    @Test
    void findAllReturnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("name"));
        PatientEntity entity = new PatientEntity();
        PatientResponse response = response(TEST_EMAIL);
        Page<PatientEntity> page = new PageImpl<>(List.of(entity), pageable, 1);

        when(patientRepository.findAll(pageable)).thenReturn(page);
        when(patientMapper.toResponse(entity)).thenReturn(response);

        Page<PatientResponse> result = patientService.findAll(pageable);

        assertThat(result.getContent()).containsExactly(response);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void deleteRemovesExistingPatient() {
        PatientEntity entity = new PatientEntity();
        when(patientRepository.findById("patient-id")).thenReturn(Optional.of(entity));

        patientService.delete("patient-id");

        verify(patientRepository).delete(entity);
    }

    @Test
    void deleteThrowsWhenPatientDoesNotExist() {
        when(patientRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.delete("missing-id"))
                .isInstanceOf(PatientNotFoundException.class);

        verify(patientRepository, never()).delete(any());
    }

    private static PatientResponse response(String email) {
        Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
        return new PatientResponse(
                "patient-id",
                "Justin Lopez",
                email,
                "123 Main St",
                LocalDate.of(1990, 1, 15),
                "123456789",
                timestamp,
                timestamp
        );
    }

}
