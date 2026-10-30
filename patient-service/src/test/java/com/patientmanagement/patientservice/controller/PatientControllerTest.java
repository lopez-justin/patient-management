package com.patientmanagement.patientservice.controller;

import com.jayway.jsonpath.JsonPath;
import com.patientmanagement.patientservice.client.BillingAccountClient;
import com.patientmanagement.patientservice.exception.BillingServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PatientControllerTest {

    private static final String BASE_URL = "/api/v1/patients";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BillingAccountClient billingAccountClient;

    @Test
    void createPatientReturnsCreatedResponse() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson("Justin Lopez", "justinlopez@example.com")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(BASE_URL + "/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Justin Lopez"))
                .andExpect(jsonPath("$.email").value("justinlopez@example.com"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void findPatientByIdReturnsPatient() throws Exception {
        String id = createPatient("justinlopez@example.com");

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Justin Lopez"))
                .andExpect(jsonPath("$.email").value("justinlopez@example.com"));
    }

    @Test
    void listPatientsReturnsPaginatedResponse() throws Exception {
        createPatient("alice@example.com", "Alice Smith");
        createPatient("bob@example.com", "Bob Smith");

        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Alice Smith"))
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    void updatePatientReturnsUpdatedPatient() throws Exception {
        String id = createPatient("justinlopez@example.com");

        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson("Justin Lopez", "justinlopez@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Justin Lopez"))
                .andExpect(jsonPath("$.email").value("justinlopez@example.com"));
    }

    @Test
    void deletePatientReturnsNoContent() throws Exception {
        String id = createPatient("justinlopez@example.com");

        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPatientWithInvalidDataReturnsBadRequest() throws Exception {
        String invalidRequest = """
                {
                  "name": "",
                  "email": "not-an-email",
                  "address": "",
                  "birthDate": "2999-01-01",
                  "phoneNumber": "123"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.address").exists())
                .andExpect(jsonPath("$.errors.birthDate").exists())
                .andExpect(jsonPath("$.errors.phoneNumber").exists());
    }

    @Test
    void createPatientWhenBillingServiceFailsReturnsServiceUnavailable() throws Exception {
        doThrow(new BillingServiceUnavailableException(new RuntimeException("down")))
                .when(billingAccountClient)
                .createBillingAccount(anyString(), anyString(), anyString());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson("Justin Lopez", "justinlopez@example.com")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Billing service unavailable"));
    }

    @Test
    void createPatientWithDuplicateEmailReturnsConflict() throws Exception {
        createPatient("justinlopez@example.com");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson("Another Justin", "JUSTINLOPEZ@example.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Email already registered"));
    }

    @Test
    void findMissingPatientReturnsNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", "missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Patient not found"));
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed request"));
    }

    private String createPatient(String email) throws Exception {
        return createPatient(email, "Justin Lopez");
    }

    private String createPatient(String email, String name) throws Exception {
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson(name, email)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private static String patientJson(String name, String email) {
        return """
                {
                  "name": "%s",
                  "email": "%s",
                  "address": "123 Main St",
                  "birthDate": "1990-01-15",
                  "phoneNumber": "123456789"
                }
                """.formatted(name, email);
    }

}
