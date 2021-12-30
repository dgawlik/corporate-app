package org.dgawlik.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.dgawlik.domain.CaseRepository;
import org.dgawlik.domain.PersonRepository;
import org.dgawlik.domain.dto.Fill;
import org.dgawlik.domain.dto.LimitedCaseView;
import org.dgawlik.domain.dto.LimitedPersonView;
import org.dgawlik.exception.NonExistingResourceException;
import org.dgawlik.service.ApprovalRulesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.dgawlik.util.Utility.extractString;

/**
 * REST api for EngineService and Repositories.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PersonApiController
        extends ApiBase {

    private final PersonRepository personRepository;

    private final CaseRepository caseRepository;

    private final ApprovalRulesService approvalRulesService;


    @GetMapping(path = "/fill")
    public Fill getPerson(@RequestAttribute DecodedJWT decodedJwt) {

        var me = personRepository
                .findByFirstNameAndLastName(
                        extractString(decodedJwt, "firstName"),
                        extractString(decodedJwt, "lastName"))
                .orElseThrow(() ->
                        new NonExistingResourceException("Person does not exist"));

        List<LimitedPersonView> colleagues = personRepository
                .findAllProjectedBy(LimitedPersonView.class);

        final var caseIds =
                me.getCaseIds() != null ?
                        me.getCaseIds() :
                        List.of();

        var cases = caseRepository
                .findAllProjectedBy(LimitedCaseView.class)
                .stream()
                .filter(v -> caseIds.contains(v.getId()))
                .collect(Collectors.toList());

        var actions = approvalRulesService.canInitiate(me.getRole());

        return new Fill(me, colleagues
                .stream()
                .toList(), cases, actions);
    }
}
