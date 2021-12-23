package org.dgawlik.service;

import org.dgawlik.domain.document.Action;
import org.dgawlik.domain.document.Role;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;


/**
 * Class guides workflow of approvals and serves
 * as authorization service for inits.
 */
@Service
public class ApprovalRulesService {

    public Set<Role> approves(Action action) {
        if (action == Action.FIRE || action == Action.HIRE) {
            return Set.of(Role.CEO);
        } else {
            return Set.of(Role.HR_MANAGER, Role.IT_MANAGER, Role.CEO);
        }
    }

    public List<Action> canInitiate(Role role){
        if(role == Role.IT || role == Role.HR){
            return List.of(Action.HIRE, Action.PRAISE);
        }
        else {
            return List.of(Action.HIRE, Action.PRAISE, Action.FIRE, Action.GIVE_RAISE);
        }
    }
}
