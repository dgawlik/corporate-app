package org.dgawlik.service.command;

import org.dgawlik.domain.PersonRepository;
import org.dgawlik.domain.document.Case;
import org.dgawlik.domain.document.Person;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.dgawlik.util.Utility.idpRequest;

public class Fire
        extends ActionExecution {

    private final RestTemplate rest;
    private final String secret;

    public Fire(PersonRepository personRepository,
            RestTemplate rest, String secret) {

        super(personRepository);
        this.rest = rest;
        this.secret = secret;
    }

    @Override
    public void execute(Case casee) {

        Optional<Person> subject = getSubjectFromCase(casee);

        assert subject.isPresent();

        promoteChildRebind(subject.get());
        personRepository.delete(subject.get());

        var request = idpRequest(
                subject
                        .get()
                        .getFirstName(),
                subject
                        .get()
                        .getLastName(),
                secret);
        var response = rest
                .postForEntity("/api/user/delete", request, String.class);


        if (response.getStatusCode() != HttpStatus.OK) {
            throw new IllegalStateException("Unable to deregister user at IDP");
        }
    }

    private Person promoteChildRebind(Person person) {

        var subordinates = personRepository.findByParentId(person.getId());

        if (!subordinates.isEmpty()) {
            subordinates.sort(PROMOTION_RULES);

            var topChild = subordinates.remove(0);

            var followUp = promoteChildRebind(topChild);
            if (followUp != null) {
                subordinates.add(followUp);
            }

            inheritGear(person, topChild);
            topChild.setChildrenIds(extractIds(subordinates));
            subordinates.forEach(sub -> sub.setParentId(topChild.getId()));

            personRepository.saveAll(subordinates);
            personRepository.save(topChild);

            return topChild;
        }

        return null;
    }

    @NotNull
    private List<String> extractIds(List<Person> subordinates) {

        return subordinates
                .stream()
                .map(Person::getId)
                .collect(Collectors.toList());
    }

    private void inheritGear(Person subject, Person top) {

        top.setParentId(subject.getParentId());
        top.setRole(subject.getRole());
        top.setSalary(subject.getSalary());
        top.setAppraisals(subject.getAppraisals());
    }
}
