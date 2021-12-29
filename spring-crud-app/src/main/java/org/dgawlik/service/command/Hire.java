package org.dgawlik.service.command;

import org.dgawlik.domain.PersonRepository;
import org.dgawlik.domain.document.Case;
import org.dgawlik.domain.document.Person;
import org.dgawlik.domain.document.Role;
import org.dgawlik.exception.IllegalApiUseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.dgawlik.util.Utility.getWithDefaultWithAppended;
import static org.dgawlik.util.Utility.idpRequest;

public class Hire
        extends ActionExecution {

    private final RestTemplate rest;
    private final String secret;

    public Hire(PersonRepository personRepository,
            RestTemplate rest, String secret) {
        super(personRepository);
        this.rest = rest;
        this.secret = secret;
    }

    @Override
    public void execute(Case casee) {
        Person subject = casee.getSubjectPerson();

        Role subjectRole = subject.getRole();
        if (subjectRole != Role.IT
            && subjectRole != Role.HR) {
            throw new IllegalApiUseException("Subject must start from the bottom");
        }

        subject.setSalary(400000.0);
        subject.setAppraisals(0);

        // silently ignoring case where there is nobody employed
        // as there would be nobody to fulfill the request
        // the last person to leave company is CEO

        // however there can be empty department
        // such department doesn't have manager
        // manager is last person to leave department
        List<Person> manager = personRepository.findByRole(
                subjectRole == Role.IT ?
                        Role.IT_MANAGER :
                        Role.HR_MANAGER
        );

        if (manager.isEmpty()) {
            List<Person> ceo = personRepository.findByRole(Role.CEO);
            subject.setParentId(ceo.get(0)
                                   .getId());
            subject.setRole(subjectRole == Role.IT ?
                    Role.IT_MANAGER :
                    Role.HR_MANAGER);

            var newChildrenIds = getWithDefaultWithAppended(ceo.get(0)
                                                               .getChildrenIds(), subject.getId());
            ceo.get(0)
               .setChildrenIds(newChildrenIds);
            personRepository.save(ceo.get(0));
        } else {
            subject.setParentId(manager.get(0)
                                       .getId());
            subject.setRole(subjectRole);
            var newChildrenIds = getWithDefaultWithAppended(manager.get(0)
                                                                   .getChildrenIds(), subject.getId());
            manager.get(0)
                   .setChildrenIds(newChildrenIds);
            personRepository.save(manager.get(0));
        }
        personRepository.save(subject);


        var request = idpRequest(subject.getFirstName(),
                subject.getLastName(), secret);
        var response = rest.postForEntity("/api/user/create", request, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new IllegalStateException("Unable to register user at IDP");
        }
    }
}
