package org.dgawlik.service.command;

import org.dgawlik.domain.PersonRepository;
import org.dgawlik.domain.document.Case;
import org.dgawlik.domain.document.Person;
import org.dgawlik.domain.document.Role;
import org.dgawlik.exception.IllegalApiUseException;

import java.util.List;

import static org.dgawlik.util.Utility.getWithDefaultWithAppended;

public class Hire extends ActionExecution {

    public Hire(PersonRepository personRepository) {
        super(personRepository);
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
                subjectRole == Role.IT ? Role.IT_MANAGER : Role.HR_MANAGER
        );

        if (manager.isEmpty()) {
            List<Person> ceo = personRepository.findByRole(Role.CEO);
            subject.setParentId(ceo.get(0)
                    .getId());
            subject.setRole(subjectRole == Role.IT ? Role.IT_MANAGER : Role.HR_MANAGER);

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
    }
}
