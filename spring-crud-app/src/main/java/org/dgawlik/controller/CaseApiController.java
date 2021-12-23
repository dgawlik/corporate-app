package org.dgawlik.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import info.schnatterer.mobynamesgenerator.MobyNamesGenerator;
import lombok.RequiredArgsConstructor;
import org.dgawlik.domain.CaseRepository;
import org.dgawlik.domain.PersonRepository;
import org.dgawlik.domain.document.Action;
import org.dgawlik.domain.document.Person;
import org.dgawlik.domain.dto.CaseInitiation;
import org.dgawlik.domain.dto.CaseUpdate;
import org.dgawlik.domain.dto.LimitedCaseView;
import org.dgawlik.exception.IllegalApiUseException;
import org.dgawlik.exception.NonExistingResourceException;
import org.dgawlik.service.EngineService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST api for EngineService and Repositories.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CaseApiController extends ApiBase {

    private final PersonRepository personRepository;

    private final CaseRepository caseRepository;

    private final EngineService engineService;


    @GetMapping(path = "/person/cases")
    public List<LimitedCaseView> getCasesForPerson(@RequestAttribute DecodedJWT decodedJwt) {
        var cases = caseRepository.findAllProjectedBy(LimitedCaseView.class)
                .stream()
                .collect(Collectors.toMap(LimitedCaseView::getId, lcv -> lcv));
        var person = personRepository.findByFirstNameAndLastName(
                        decodedJwt.getClaim("firstName")
                                .asString(),
                        decodedJwt.getClaim("lastName")
                                .asString())
                .orElseThrow(() -> new IllegalApiUseException("Person does not exist"));

        var caseIds = person.getCaseIds() == null ?
                List.of() : person.getCaseIds();

        return caseIds
                .stream()
                .map(cases::get)
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/case/{caseId}")
    public LimitedCaseView getCase(@PathVariable String caseId) {
        return caseRepository.findById(caseId, LimitedCaseView.class)
                .orElseThrow(() ->
                        new NonExistingResourceException("Case does not exist"));
    }

    @PostMapping(path = "/case")
    @ResponseStatus(HttpStatus.CREATED)
    public String initiateCase(@RequestBody @Valid CaseInitiation caseInitiation,
                               BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new IllegalApiUseException("Some mandatory field is null");
        }

        var onBehalf = personRepository.findById(
                        caseInitiation.getOnBehalfId())
                .orElseThrow(() -> new NonExistingResourceException(
                        "Initiating user does not exist"));

        Person subject = null;
        if (caseInitiation.getAction() != Action.HIRE) {
            subject = personRepository.findById(
                            caseInitiation.getSubject()
                                    .getId())
                    .orElseThrow(() -> new NonExistingResourceException(
                            "Initiating user does not exist"));
        } else {
            if (caseInitiation.getSubject()
                    .getFirstName() == null ||
                    caseInitiation.getSubject()
                            .getLastName() == null ||
                    caseInitiation.getSubject()
                            .getRole() == null) {
                throw new IllegalApiUseException("Some mandatory field for subject is null");
            }

            subject = Person.builder()
                    .firstName(caseInitiation.getSubject()
                            .getFirstName())
                    .lastName(caseInitiation.getSubject()
                            .getLastName())
                    .role(caseInitiation.getSubject()
                            .getRole())
                    .id(MobyNamesGenerator.getRandomName())
                    .build();
        }

        engineService.initiate(onBehalf, subject, caseInitiation.getAction(),
                caseInitiation.getJustification());

        return "created";
    }


    /**
     * Maybe it should look like /case/{caseId} to be
     * more correct but I haven't seen that this way
     * is strictly prohibited (and it's easier).
     *
     * @param caseUpdate
     * @param bindingResult
     * @return
     */
    @PutMapping(path = "/case")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String updateCase(@RequestBody @Valid CaseUpdate caseUpdate,
                             BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new IllegalApiUseException("Some mandatory field is null");
        }

        var onBehalf = personRepository.findById(
                        caseUpdate.getOnBehalfId())
                .orElseThrow(() -> new NonExistingResourceException(
                        "Initiating user does not exist"));

        var casee = caseRepository.findById(caseUpdate.getCaseId())
                .orElseThrow(() -> new NonExistingResourceException(
                        "Case does not exist"));

        if (caseUpdate.getApprove()) {
            engineService.approve(onBehalf, casee, caseUpdate.getJustification());
        } else {
            engineService.reject(onBehalf, casee, caseUpdate.getJustification());
        }

        return "updated";
    }
}
