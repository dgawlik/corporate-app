package org.dgawlik;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dgawlik.domain.CaseRepository;
import org.dgawlik.domain.PersonRepository;
import org.dgawlik.domain.document.Action;
import org.dgawlik.domain.document.Case;
import org.dgawlik.domain.document.Person;
import org.dgawlik.domain.document.Role;
import org.dgawlik.exception.IllegalApiUseException;
import org.dgawlik.service.ActionExecutorService;
import org.dgawlik.service.EngineService;
import org.dgawlik.util.SetupTeardownMongo;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;


@ExtendWith(SetupTeardownMongo.class)
@SpringBootTest
@ActiveProfiles("testing")
class BusinessLogicTests {

    @Autowired
    private EngineService engineService;

    @Autowired
    private ActionExecutorService actionExecutorService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CaseRepository caseRepository;

    @MockBean
    private RestTemplate restTemplate;

    @Value("classpath:/fixture.json")
    private Resource fixture;

    private ObjectMapper om = new ObjectMapper();

    @BeforeEach
    public void initEach() throws
                           Exception {
        var fix = om.readValue(fixture.getFile(),
                new TypeReference<Map<String, List<Person>>>() {
                });

        personRepository.saveAll(fix.get("people"));
    }

    @AfterEach
    public void afterEach() {
        personRepository.deleteAll();
        caseRepository.deleteAll();
    }

    @Test
    @DisplayName("Initiating while not able to fulfill promotes case to superior")
    void test1() {
        var james = person("d").get();
        var subject = person("g").get();
        var casee = engineService.initiate(james, subject, Action.PRAISE, "He's mean");

        var adam = person("b").get();
        james = person("d").get();

        assertThat(james.getCaseIds()
                        .size()).isEqualTo(1);
        assertThat(adam.getCaseIds()
                       .size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Initiating while able to fulfill executes the action")
    void test2() {
        var greg = person("a").get();

        assertThat(greg.getAppraisals()).isEqualTo(1);
        var casee = engineService.initiate(greg, greg, Action.PRAISE, "He's mean");
        greg = person("a").get();
        assertThat(greg.getAppraisals()).isEqualTo(2);
    }

    @Test
    @DisplayName("Approval chain with fulfillment")
    void test3() {
        var james = person("d").get();
        var frank = person("g").get();

        assertThat(frank.getAppraisals()).isEqualTo(1);
        var casee = engineService.initiate(james, frank, Action.PRAISE, "nice");

        var adam = person("b").get();
        engineService.approve(adam, casee, "nice 2");

        frank = person("g").get();
        assertThat(frank.getAppraisals()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rejection in chain is final")
    void test4() {
        var james = person("d").get();
        var frank = person("g").get();

        var casee = engineService.initiate(james, frank, Action.PRAISE, "nice");

        var adam = person("b").get();
        var case2 = engineService.reject(adam, casee, "i disagree");

        var greg = person("a").get();
        assertThatExceptionOfType(IllegalApiUseException.class)
                .isThrownBy(() -> {
                    engineService.approve(greg, case2, "i agree");
                })
                .withMessage("Case already has been rejected");

    }

    @Nested
    @DisplayName("With approval/rejection working")
    class ActionExecutionTests {

        @Test
        @DisplayName("FIRE execution works")
        public void test1() {
            var greg = person("a").get();

            var casee = Case.builder()
                            .action(Action.FIRE)
                            .subjectPerson(greg)
                            .build();

            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.ok()
                                              .build());

            actionExecutorService.perform(casee);

            var meg = person("c").get();
            assertThat(meg.getRole()).isEqualTo(Role.CEO);
            var frank = person("g").get();
            assertThat(frank.getRole()).isEqualTo(Role.HR_MANAGER);
            var frank2 = person("f").get();
            assertThat(frank2.getParentId()).isEqualTo("g");
            assertThat(frank.getParentId()).isEqualTo("c");
            assertThat(person("a")).isEmpty();
        }

        @Test
        @DisplayName("HIRE execution works")
        void test2() {
            var newPerson = Person.builder()
                                  .firstName("John")
                                  .lastName("Doe")
                                  .role(Role.IT)
                                  .age(20)
                                  .build();

            var casee = Case.builder()
                            .subjectPerson(newPerson)
                            .action(Action.HIRE)
                            .build();

            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.ok()
                                              .build());

            actionExecutorService.perform(casee);

            var its = personRepository.findByRole(Role.IT);
            assertThat(its.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("HIRE execution works | empty department")
        void test3() {
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.ok()
                                              .build());

            fire("d");
            fire("e");
            fire("b");

            var newPerson = Person.builder()
                                  .firstName("John")
                                  .lastName("Doe")
                                  .id("h")
                                  .role(Role.IT)
                                  .age(20)
                                  .build();

            var casee = Case.builder()
                            .subjectPerson(newPerson)
                            .action(Action.HIRE)
                            .build();

            actionExecutorService.perform(casee);

            var its = personRepository.findByRole(Role.IT);
            assertThat(its.size()).isEqualTo(0);
            var itms = personRepository.findByRole(Role.IT_MANAGER);
            assertThat(itms.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("PRAISE execution works")
        void test4() {

            var greg = person("a").get();
            assertThat(greg.getAppraisals()).isEqualTo(1);

            var casee = Case.builder()
                            .subjectPerson(greg)
                            .action(Action.PRAISE)
                            .build();

            actionExecutorService.perform(casee);

            greg = person("a").get();
            assertThat(greg.getAppraisals()).isEqualTo(2);
        }

        @Test
        @DisplayName("GIVE_RAISE execution works")
        void test5() {

            var greg = person("a").get();
            var prevSalary = greg.getSalary();

            var casee = Case.builder()
                            .subjectPerson(greg)
                            .action(Action.GIVE_RAISE)
                            .build();

            actionExecutorService.perform(casee);

            greg = person("a").get();
            assertThat(greg.getSalary()).isEqualTo(1.1 * prevSalary);
        }
    }


    @NotNull
    private Optional<Person> person(String id) {
        return personRepository.findById(id);
    }

    private void fire(String personId) {
        var p = person(personId).get();

        var casee = Case.builder()
                        .action(Action.FIRE)
                        .subjectPerson(p)
                        .build();

        actionExecutorService.perform(casee);
    }

}
