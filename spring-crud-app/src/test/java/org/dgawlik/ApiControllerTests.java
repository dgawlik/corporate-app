package org.dgawlik;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.dgawlik.domain.CaseRepository;
import org.dgawlik.domain.PersonRepository;
import org.dgawlik.domain.document.Action;
import org.dgawlik.domain.document.Person;
import org.dgawlik.domain.document.Role;
import org.dgawlik.domain.dto.CaseInitiation;
import org.dgawlik.domain.dto.CaseUpdate;
import org.dgawlik.domain.dto.LimitedPersonView;
import org.dgawlik.exception.NonExistingResourceException;
import org.dgawlik.service.EngineService;
import org.dgawlik.service.KeyProvider;
import org.dgawlik.service.ApprovalRulesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.Cookie;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest
@ActiveProfiles("testing")
class ApiControllerTests {

    @TestConfiguration
    public static class Config {

        @Bean
        public KeyProvider keyProvider() {
            return new KeyProvider();
        }
    }

    @MockBean
    private EngineService engineService;

    @MockBean
    private PersonRepository personRepository;

    @MockBean
    private CaseRepository caseRepository;

    @MockBean
    private ApprovalRulesService approvalRulesService;

    @Autowired
    private KeyProvider keyProvider;

    @Autowired
    private MockMvc mockMvc;


    private Person greg;
    private Person adam;
    private Person meg;

    @BeforeEach
    public void beforeAll() {
        greg = Person.builder()
                .firstName("Gregory")
                .lastName("Peck")
                .age(40)
                .salary(120_000.0)
                .childrenIds(List.of("b", "c"))
                .role(Role.CEO)
                .build();

        adam = Person.builder()
                .firstName("Adam")
                .lastName("Sandler")
                .age(30)
                .salary(80_000.0)
                .childrenIds(List.of("d", "e"))
                .parentId("a")
                .role(Role.IT_MANAGER)
                .build();

        meg = Person.builder()
                .firstName("Meg")
                .lastName("Ryan")
                .age(30)
                .salary(90_000.0)
                .childrenIds(List.of("e", "g"))
                .parentId("a")
                .role(Role.HR_MANAGER)
                .build();
    }

    @Test
    @DisplayName("/api/fill status OK")
    void test1() throws Exception {
        var token = getToken("Gregory", "Peck", "CORP LOGIN");
        defaultMocks();

        mockMvc.perform(get("/api/fill")
                        .cookie(new Cookie("CORP-ID", token)))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk());

        verify(approvalRulesService).canInitiate(any());
        verify(personRepository).findAllProjectedBy(any());
        verify(caseRepository).findAllProjectedBy(any());
        verify(personRepository).findByFirstNameAndLastName(any(), any());
    }

    @Test
    @DisplayName("/api/fill unauthorized")
    void test2() throws Exception {
        mockMvc.perform(get("/api/fill"))
                .andExpect(MockMvcResultMatchers.status()
                        .isUnauthorized())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content()
                        .string("No credentials found."));
    }

    @Test
    @DisplayName("/api/fill unverifiable JWT")
    void test3() throws Exception {
        var token = getToken("Gregory", "Peck", "OTHER LOGIN");
        mockMvc.perform(get("/api/fill")
                        .cookie(new Cookie("CORP-ID", token)))
                .andExpect(MockMvcResultMatchers.status()
                        .isUnauthorized())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content()
                        .string("Token could not be verified."));
    }

    @Test
    @DisplayName("/api/fill non existing person")
    void test4() throws Exception {
        var token = getToken("Gregory", "Peck", "CORP LOGIN");
        when(personRepository.findByFirstNameAndLastName(any(), any()))
                .thenThrow(new NonExistingResourceException("Non existing person."));


        mockMvc.perform(get("/api/fill")
                        .cookie(new Cookie("CORP-ID", token)))
                .andExpect(MockMvcResultMatchers.status()
                        .isNotFound())
                .andExpect(MockMvcResultMatchers.content()
                        .contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content()
                        .string("Non existing person."));

    }

    @Test
    @DisplayName("/api/person/cases status OK")
    void test5() throws Exception {
        var token = getToken("Gregory", "Peck", "CORP LOGIN");
        when(personRepository.findByFirstNameAndLastName(any(), any()))
                .thenReturn(Optional.of(Person.builder()
                        .firstName("Greg")
                        .build()));
        when(caseRepository.findAllProjectedBy(any()))
                .thenReturn(List.of());


        mockMvc.perform(get("/api/person/cases")
                        .cookie(new Cookie("CORP-ID", token)))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk());
    }

    @Test
    @DisplayName("/api/case/nonExisting status OK")
    void test6() throws Exception {
        var token = getToken("Gregory", "Peck", "CORP LOGIN");

        when(caseRepository.findById(any()))
                .thenThrow(new NonExistingResourceException("Case not exists."));


        mockMvc.perform(get("/api/case/nonExisting")
                        .cookie(new Cookie("CORP-ID", token)))
                .andExpect(MockMvcResultMatchers.status()
                        .isNotFound());
    }

    @Test
    @DisplayName("POST /api/case validation")
    void test7() throws Exception {
        var token = getToken("Gregory", "Peck", "CORP LOGIN");

        var caseInitiation = "{\n" +
                "  \"onBehalfId\": \"a\",\n" +
                "  \"action\": \"HIRE\",\n" +
                "  \"justification\": \"a\"\n" +
                "}";


        mockMvc.perform(post("/api/case")
                        .cookie(new Cookie("CORP-ID", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(caseInitiation))
                .andExpect(MockMvcResultMatchers.status()
                        .isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/case validation")
    void test8() throws Exception {
        var token = getToken("Gregory", "Peck", "CORP LOGIN");

        var caseUpdate = new CaseUpdate("a", null,
                false, "a");


        mockMvc.perform(put("/api/case")
                        .cookie(new Cookie("CORP-ID", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(caseUpdate)))
                .andExpect(MockMvcResultMatchers.status()
                        .isBadRequest());
    }

    private void defaultMocks() {
        when(approvalRulesService.canInitiate(any()))
                .thenReturn(null);
        when(personRepository.findByFirstNameAndLastName(eq("Gregory"),
                eq("Peck"))).thenReturn(Optional.of(greg));
        when(personRepository.findAllProjectedBy(any()))
                .thenReturn(List.of());
        when(caseRepository.findAllProjectedBy(any()))
                .thenReturn(List.of());
    }

    private String getToken(String firstName, String lastName, String iss) {

        return JWT.create()
                .withClaim("firstName", firstName)
                .withClaim("lastName", lastName)
                .withExpiresAt(new Date(
                        Instant.now()
                                .plus(1, ChronoUnit.DAYS)
                                .toEpochMilli()))
                .withIssuer(iss)
                .sign(Algorithm.RSA256(keyProvider));
    }

}
