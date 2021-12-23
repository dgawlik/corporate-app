package org.dgawlik.service.command;

import org.dgawlik.domain.PersonRepository;
import org.dgawlik.domain.document.Case;
import org.dgawlik.domain.document.Person;

import java.util.Optional;

public class GiveRaise extends ActionExecution {

    public GiveRaise(PersonRepository personRepository) {
        super(personRepository);
    }

    @Override
    public void execute(Case casee) {
        Optional<Person> subject = getSubjectFromCase(casee);

        subject.get()
                .setSalary(subject.get()
                        .getSalary() * 1.1);
        personRepository.save(subject.get());
    }
}
