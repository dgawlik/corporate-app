package org.dgawlik;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.util.Map;

public class Client {

    public static void main(String[] args) {

        var token = BCrypt.withDefaults()
                          .hashToString(12, "secret".toCharArray());


        var restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:8081")
                .defaultHeader("Authorization", "Bearer " + token)
                .build();

        var response = restTemplate.postForEntity("/api/user/delete",
                Map.of("firstName", "dominik"), String.class);

        System.out.println(response.getStatusCode());
    }
}
