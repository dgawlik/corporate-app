package org.dgawlik.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Utility {

    @NotNull
    public static List<String> getWithDefaultWithAppended(List<String> cases, String id) {

        if (cases == null)
            cases = new ArrayList<>();
        else
            cases = new ArrayList<>(cases);
        cases.add(id);
        return cases;
    }

    public static HttpEntity<Map<String, String>> idpRequest(String firstName,
            String lastName, String secret) {

        var token = BCrypt
                .withDefaults()
                .hashToString(12, secret.toCharArray());
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return new HttpEntity<>(Map.of("firstName", firstName,
                "lastName", lastName), headers);
    }

    public static String extractString(DecodedJWT decodedJwt, String firstName) {

        return decodedJwt
                .getClaim(firstName)
                .asString();
    }
}
