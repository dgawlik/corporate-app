package org.dgawlik;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dgawlik.domain.CaseRepository;
import org.dgawlik.domain.PersonRepository;
import org.dgawlik.domain.document.Action;
import org.dgawlik.domain.document.Case;
import org.dgawlik.domain.document.Person;
import org.dgawlik.domain.document.Role;
import org.dgawlik.domain.dto.LimitedCaseView;
import org.dgawlik.util.SetupTeardownMongo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SetupTeardownMongo.class)
@DataMongoTest
@ActiveProfiles("testing")
class MongoTests {

    private Person greg;
    private Person monica;
    private Person frank;
    private Person adam;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Value("classpath:/fixture.json")
    private Resource fixture;

    private ObjectMapper om = new ObjectMapper();

    @BeforeEach
    public void initEach() throws Exception {

        var fix = om.readValue(fixture.getFile(),
                new TypeReference<Map<String, List<Person>>>() {
                });

        personRepository.saveAll(fix.get("people"));

        greg = personRepository.findByFirstNameAndLastName("Gregory", "Peck")
                .get();
        frank = personRepository.findByFirstNameAndLastName("Frank", "Moody")
                .get();
        adam = personRepository.findByFirstNameAndLastName("Adam", "Sandler")
                .get();
        monica = personRepository.findByFirstNameAndLastName("Monica", "Belluci")
                .get();

        var firstCase = Case.builder()
                .id("theId")
                .approved(true)
                .subjectPerson(monica)
                .initiatingPerson(frank)
                .action(Action.HIRE)
                .timestamp(Instant.now())
                .justification("We need more workers")
                .build();

        var secondCase = Case.builder()
                .id("theId")
                .approved(true)
                .subjectPerson(monica)
                .initiatingPerson(adam)
                .action(Action.HIRE)
                .timestamp(Instant.now())
                .thread(firstCase)
                .justification("Approved")
                .build();

        var thirdCase = Case.builder()
                .id("theId")
                .approved(true)
                .subjectPerson(monica)
                .initiatingPerson(greg)
                .action(Action.HIRE)
                .timestamp(Instant.now())
                .thread(secondCase)
                .justification("We have no resources")
                .build();

        caseRepository.save(thirdCase);
    }

    @AfterEach
    public void teardownEach() {
        personRepository.deleteAll();
        caseRepository.deleteAll();
    }

    @Test
    @DisplayName("Person DB deserialization is working")
    void test1(@Autowired PersonRepository repository) {
        assertThat(repository.findAll()
                .size())
                .isEqualTo(7);

        var greg = repository.findByFirstNameAndLastName("Gregory", "Peck");

        assertThat(greg).isPresent();

        var shouldBe = Person.builder()
                .id("a")
                .firstName("Gregory")
                .lastName("Peck")
                .age(40)
                .salary(120_000.0)
                .role(Role.CEO)
                .childrenIds(List.of("b", "c"))
                .appraisals(1)
                .build();
        assertThat(greg).contains(shouldBe);
    }

    @Test
    @DisplayName("Nested Cases DB serialization/deserialization is working")
    void test2() {

        var casee = caseRepository.findById("theId");
        assertThat(casee).isPresent();

        assertThat(casee.get()
                .getJustification()).isEqualTo("We have no resources");
        assertThat(casee.get()
                .getInitiatingPerson()
                .getId()).isEqualTo(greg
                .getId());
        assertThat(casee.get()
                .getSubjectPerson()
                .getId()).isEqualTo(monica
                .getId());

        assertThat(casee.get()
                .getThread()).isNotNull();

        var nested1 = casee.get()
                .getThread();
        assertThat(nested1.getJustification()).isEqualTo("Approved");
        assertThat(nested1.getInitiatingPerson()
                .getId()).isEqualTo(adam
                .getId());
        assertThat(nested1.getSubjectPerson()
                .getId()).isEqualTo(monica
                .getId());

        assertThat(nested1.getThread()).isNotNull();

        var nested2 = nested1.getThread();
        assertThat(nested2.getJustification()).isEqualTo("We need more workers");
        assertThat(nested2.getInitiatingPerson()
                .getId()).isEqualTo(frank
                .getId());
        assertThat(nested2.getSubjectPerson()
                .getId()).isEqualTo(monica
                .getId());
    }

    @Test
    @DisplayName("Case projection is applied recursively")
    void test3() {
        var caseView = caseRepository.findById("theId", LimitedCaseView.class);
        assertThat(caseView).isPresent();

        assertThat(caseView.get()
                .getJustification()).isEqualTo("We have no resources");
        assertThat(caseView.get()
                .getInitiatingPerson()
                .getId()).isEqualTo(greg
                .getId());
        assertThat(caseView.get()
                .getSubjectPerson()
                .getId()).isEqualTo(monica
                .getId());

        assertThat(caseView.get()
                .getThread()).isNotNull();

        var nested1 = caseView.get()
                .getThread();
        assertThat(nested1.getJustification()).isEqualTo("Approved");
        assertThat(nested1.getInitiatingPerson()
                .getId()).isEqualTo(adam
                .getId());
        assertThat(nested1.getSubjectPerson()
                .getId()).isEqualTo(monica
                .getId());

        assertThat(nested1.getThread()).isNotNull();

        var nested2 = nested1.getThread();
        assertThat(nested2.getJustification()).isEqualTo("We need more workers");
        assertThat(nested2.getInitiatingPerson()
                .getId()).isEqualTo(frank
                .getId());
        assertThat(nested2.getSubjectPerson()
                .getId()).isEqualTo(monica
                .getId());
    }
}
