package org.dgawlik.service.command;

import org.dgawlik.domain.PersonRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ActionExecutionConfig {

    private final PersonRepository personRepository;
    private final String idpHost;
    private final Integer idpPort;
    private final String idpApiSecret;

    public ActionExecutionConfig(PersonRepository personRepository,
            @Value("${idp.server.host.back:localhost}") String idpHost,
            @Value("${idp.server.port:8081}") Integer idpPort,
            @Value("${idp.api.secret:secret}") String idpApiSecret) {

        this.personRepository = personRepository;
        this.idpHost = idpHost;
        this.idpPort = idpPort;
        this.idpApiSecret = idpApiSecret;
    }

    @Bean
    public ActionExecution fireCommand() {

        return new Fire(personRepository, idpRest(), idpApiSecret);
    }

    @Bean
    public ActionExecution giveRaiseCommand() {

        return new GiveRaise(personRepository);
    }

    @Bean
    public ActionExecution hireCommand() {

        return new Hire(personRepository, idpRest(), idpApiSecret);
    }

    @Bean
    public ActionExecution praiseCommand() {

        return new Praise(personRepository);
    }

    @Bean
    public RestTemplate idpRest() {

        return new RestTemplateBuilder()
                .rootUri("http://" + idpHost + ":" + idpPort)
                .build();
    }
}
