package org.dgawlik.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.dgawlik.repository.InMemoryStore;
import org.dgawlik.security.KeyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Controller
public class UserController {

    @Autowired
    InMemoryStore store;

    @Autowired
    KeyProvider kp;

    @GetMapping(value = "/authenticate", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<String> getUser(@AuthenticationPrincipal String uid,
                                          @RequestParam("callback") String callback)
            throws IOException {
        var user = store.getByEmail(uid);

        var algo = Algorithm.RSA256(kp);
        var token = JWT.create()
                .withClaim("firstName", user.getFirstName())
                .withClaim("lastName", user.getLastName())
                .withIssuer("CORP LOGIN")
                .withExpiresAt(new Date(Instant.now()
                        .plus(1, ChronoUnit.DAYS)
                        .toEpochMilli()))
                .sign(algo);

        HttpCookie cookie = ResponseCookie.from("CORP-ID", token)
                .maxAge(Duration.of(1, ChronoUnit.HOURS))
                .path("/")
                .build();

        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                .header(HttpHeaders.LOCATION, callback)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("ok");
    }

    @GetMapping("/login")
    String login() {
        return "login-page";
    }

    @GetMapping("/logout")
    ResponseEntity<String> remoteLogout(@RequestParam String callback,
                                        HttpSession session) {

        session.invalidate();

        ResponseCookie cookie = ResponseCookie.from("JSESSIONID", null)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                .header("Set-Cookie", cookie.toString())
                .header("Location", callback)
                .build();
    }
}
