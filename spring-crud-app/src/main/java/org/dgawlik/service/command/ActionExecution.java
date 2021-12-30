package org.dgawlik.service.command;

import lombok.RequiredArgsConstructor;
import org.dgawlik.domain.PersonRepository;
import org.dgawlik.domain.document.Case;
import org.dgawlik.domain.document.Person;
import org.dgawlik.exception.IllegalApiUseException;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class ActionExecution {

    protected static final Comparator<Person> PROMOTION_RULES = Comparator
            .comparing(Person::getAge, Comparator.reverseOrder())
            .thenComparing(Person::getAppraisals, Comparator.reverseOrder())
            .thenComparing(Person::getId);

    protected final PersonRepository personRepository;

    public abstract void execute(Case casee);

    @NotNull
    protected Optional<Person> getSubjectFromCase(Case aCase) {

        var subject = personRepository.findById(aCase
                .getSubjectPerson()
                .getId());

        if (subject.isEmpty()) {
            throw new IllegalApiUseException("Case subject no longer exists");
        }
        return subject;
    }
}
