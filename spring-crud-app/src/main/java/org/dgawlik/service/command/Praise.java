package org.dgawlik.service.command;

import org.dgawlik.domain.PersonRepository;
import org.dgawlik.domain.document.Case;
import org.dgawlik.domain.document.Person;

import java.util.Optional;

public class Praise extends ActionExecution {

    public Praise(PersonRepository personRepository) {
        super(personRepository);
    }

    @Override
    public void execute(Case casee) {
        Optional<Person> subject = getSubjectFromCase(casee);

        subject.get()
                .setAppraisals(subject.get()
                        .getAppraisals() + 1);
        personRepository.save(subject.get());
    }
}
