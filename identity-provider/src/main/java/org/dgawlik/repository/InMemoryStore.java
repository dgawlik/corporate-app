package org.dgawlik.repository;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.schnatterer.mobynamesgenerator.MobyNamesGenerator;
import org.dgawlik.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryStore {
    private List<User> store;

    @Value("classpath:/users.json")
    Resource resource;

    @Autowired
    ObjectMapper mapper;

    @PostConstruct
    public void init() throws
                       IOException {
        var token = new TypeReference<Map<String, List<User>>>() {
        };
        store = mapper.readValue(resource.getURL(), token)
                      .get("users");
    }

    public User getByEmail(String email) {
        return store.stream()
                    .filter(u -> u.getEmail()
                                  .equals(email))
                    .findAny()
                    .orElse(null);
    }

    public void delete(String firstName, String lastName) {
        store.removeIf(u -> u.getFirstName()
                             .equals(firstName) &&
                            u.getLastName()
                             .equals(lastName));
        System.out.printf(">>> Deregistered user, firstName: %s, lastName: %s\n",
                firstName, lastName);
    }

    public void add(String firstName, String lastName) {
        var id = MobyNamesGenerator.getRandomName();

        store.add(User.builder()
                      .firstName(firstName)
                      .lastName(lastName)
                      .email(id + "@corp.com")
                      .password(BCrypt.withDefaults()
                                      .hashToString(12, id.toCharArray()))
                      .build());

        System.out.printf(">>> Registered user, email: %s, password: %s\n",
                id + "@corp.com", id);
    }
}
