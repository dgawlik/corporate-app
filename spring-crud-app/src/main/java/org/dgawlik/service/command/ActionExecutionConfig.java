package org.dgawlik.service.command;

import lombok.RequiredArgsConstructor;
import org.dgawlik.domain.PersonRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ActionExecutionConfig {

    private final PersonRepository personRepository;

    @Bean
    public ActionExecution fireCommand() {
        return new Fire(personRepository);
    }

    @Bean
    public ActionExecution giveRaiseCommand() {
        return new GiveRaise(personRepository);
    }

    @Bean
    public ActionExecution hireCommand() {
        return new Hire(personRepository);
    }

    @Bean
    public ActionExecution praiseCommand() {
        return new Praise(personRepository);
    }
}
