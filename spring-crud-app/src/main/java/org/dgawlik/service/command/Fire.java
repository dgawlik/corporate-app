package org.dgawlik.service.command;

import org.dgawlik.domain.PersonRepository;
import org.dgawlik.domain.document.Case;
import org.dgawlik.domain.document.Person;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Fire extends ActionExecution {

    public Fire(PersonRepository personRepository) {
        super(personRepository);
    }

    @Override
    public void execute(Case casee) {
        Optional<Person> subject = getSubjectFromCase(casee);

        promoteChildRebind(subject.get());
        personRepository.delete(subject.get());
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
        return subordinates.stream()
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
