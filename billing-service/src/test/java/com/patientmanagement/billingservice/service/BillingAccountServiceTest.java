package com.patientmanagement.billingservice.service;

import com.patientmanagement.billingservice.entity.BillingAccountEntity;
import com.patientmanagement.billingservice.entity.BillingAccountStatus;
import com.patientmanagement.billingservice.repository.BillingAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillingAccountServiceTest {

    @Mock
    private BillingAccountRepository repository;

    @InjectMocks
    private BillingAccountService service;

    @Test
    void createPersistsNormalizedActiveAccount() {
        when(repository.findByPatientId("patient-id")).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(BillingAccountEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BillingAccountEntity result = service.create(
                " patient-id ",
                " Justin Lopez ",
                " JustinLopez@Example.COM ");

        ArgumentCaptor<BillingAccountEntity> captor =
                ArgumentCaptor.forClass(BillingAccountEntity.class);
        verify(repository).saveAndFlush(captor.capture());
        BillingAccountEntity saved = captor.getValue();

        assertThat(result).isSameAs(saved);
        assertThat(saved.getPatientId()).isEqualTo("patient-id");
        assertThat(saved.getPatientName()).isEqualTo("Justin Lopez");
        assertThat(saved.getPatientEmail()).isEqualTo("justinlopez@example.com");
        assertThat(saved.getStatus()).isEqualTo(BillingAccountStatus.ACTIVE);
    }

    @Test
    void createReturnsExistingAccountForSamePatient() {
        BillingAccountEntity existing = new BillingAccountEntity();
        existing.setId("billing-id");
        existing.setPatientId("patient-id");
        when(repository.findByPatientId("patient-id")).thenReturn(Optional.of(existing));

        BillingAccountEntity result = service.create("patient-id", "Justin Lopez", "a@example.com");

        assertThat(result).isSameAs(existing);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void createRejectsBlankFields() {
        assertThatThrownBy(() -> service.create(" ", "Justin Lopez", "a@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("patient_id");

        verify(repository, never()).findByPatientId(any());
    }
}
