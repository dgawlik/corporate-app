package org.dgawlik.service;

import info.schnatterer.mobynamesgenerator.MobyNamesGenerator;
import lombok.RequiredArgsConstructor;
import org.dgawlik.domain.CaseRepository;
import org.dgawlik.domain.PersonRepository;
import org.dgawlik.domain.document.Action;
import org.dgawlik.domain.document.Case;
import org.dgawlik.domain.document.Person;
import org.dgawlik.exception.IllegalApiUseException;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static org.dgawlik.util.Utility.getWithDefaultWithAppended;


/**
 * Class driving approval workflow. It is as follows:
 * <p>
 * initiate ---> [approve]* ----> done
 * or
 * initiate ---> [approve]* ----> [reject]
 */
@Service
@RequiredArgsConstructor
public class EngineService {

    private final CaseRepository caseRepository;
    private final PersonRepository personRepository;
    private final ApprovalRulesService approvalRulesService;
    private final ActionExecutorService actionExecutorService;


    public Case initiate(Person onBehalf, Person subject, Action action,
            String justification) {

        if (!approvalRulesService
                .canInitiate(onBehalf.getRole())
                .contains(action)) {
            throw new IllegalApiUseException("Initiator can't start " + action);
        }

        var root = Case
                .builder()
                .id(MobyNamesGenerator.getRandomName())
                .approved(true)
                .subjectPerson(subject)
                .initiatingPerson(onBehalf)
                .action(action)
                .timestamp(Instant.now())
                .justification(justification)
                .build();

        var cases = getWithDefaultWithAppended(
                onBehalf.getCaseIds(), root.getId());

        caseRepository.save(root);
        onBehalf.setCaseIds(cases);
        personRepository.save(onBehalf);

        if (notInPositionToPerform(action, onBehalf)) {
            updateBossMailbox(onBehalf, root.getId());
        } else {
            perform(root);
            root.setDone(true);
            caseRepository.save(root);
        }

        return root;
    }

    public Case approve(Person onBehalf, Case casee, String justification) {

        if (Boolean.FALSE.equals(casee.getApproved())) {
            throw new IllegalApiUseException("Case already has been rejected");
        }

        if (Boolean.TRUE.equals(casee.getDone())) {
            throw new IllegalApiUseException("Cannot approve already fulfilled case");
        }

        // note that when initiator is
        // in position to perform, the action is already fulfilled
        // in initiate() step
        if (casee
                .getInitiatingPerson()
                .getId()
                .equals(onBehalf.getId())) {
            throw new IllegalApiUseException("Can't approve your own case");
        }

        if (onBehalf.getCaseIds() == null ||
            !onBehalf
                    .getCaseIds()
                    .contains(casee.getId())) {
            throw new IllegalApiUseException("Person not authorized to approve case");
        }


        var newCase = nest(casee)
                .approved(true)
                .timestamp(Instant.now())
                .initiatingPerson(onBehalf)
                .justification(justification)
                .timestamp(Instant.now())
                .build();

        caseRepository.save(newCase);

        if (notInPositionToPerform(newCase.getAction(), onBehalf)) {
            updateBossMailbox(onBehalf, newCase.getId());
        } else {
            perform(newCase);
            newCase.setDone(true);
            caseRepository.save(newCase);
        }

        return newCase;
    }

    public Case reject(Person onBehalf, Case casee, String justification) {

        if (Boolean.FALSE.equals(casee.getApproved())) {
            throw new IllegalApiUseException("Case already has been rejected");
        }

        if (onBehalf.getCaseIds() == null ||
            !onBehalf
                    .getCaseIds()
                    .contains(casee.getId())) {
            throw new IllegalApiUseException("Person not authorized to reject case");
        }

        var newCase = nest(casee)
                .approved(false)
                .timestamp(Instant.now())
                .initiatingPerson(onBehalf)
                .justification(justification)
                .timestamp(Instant.now())
                .build();

        caseRepository.save(newCase);
        return newCase;
    }

    private Case.CaseBuilder nest(Case casee) {

        return Case
                .builder()
                .action(casee.getAction())
                .subjectPerson(casee.getSubjectPerson())
                .id(casee.getId())
                .thread(casee);
    }


    private void perform(Case casee) {

        actionExecutorService.perform(casee);
    }

    private boolean notInPositionToPerform(Action action, Person onBehalf) {

        return !approvalRulesService
                .approves(action)
                .contains(onBehalf.getRole());
    }

    /**
     * Superior sees case in his dashboard.
     */
    private void updateBossMailbox(Person onBehalf, String rootId) {

        var boss = personRepository.findById(onBehalf.getParentId());

        assert boss.isPresent();

        var newCases = getWithDefaultWithAppended(boss
                .get()
                .getCaseIds(), rootId);
        boss
                .get()
                .setCaseIds(newCases);
        personRepository.save(boss.get());
    }
}
