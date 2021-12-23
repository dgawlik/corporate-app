package org.dgawlik.service;

import lombok.RequiredArgsConstructor;
import org.dgawlik.domain.document.Case;
import org.dgawlik.service.command.ActionExecution;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActionExecutorService {
    private final ActionExecution fireCommand;
    private final ActionExecution hireCommand;
    private final ActionExecution giveRaiseCommand;
    private final ActionExecution praiseCommand;


    public void perform(Case casee) {
        (switch (casee.getAction()) {
            case HIRE -> hireCommand;
            case FIRE -> fireCommand;
            case GIVE_RAISE -> giveRaiseCommand;
            case PRAISE -> praiseCommand;
        }).execute(casee);
    }
}
