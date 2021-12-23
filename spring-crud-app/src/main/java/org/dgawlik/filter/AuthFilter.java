package org.dgawlik.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.dgawlik.service.KeyProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The simplest way possible to enforce authentication
 * for guarded paths. Dashboard requires JWT in cookie
 * otherwise it redirects to identity component.
 */
@Component
public class AuthFilter implements Filter {

    private final KeyProvider keyProvider;
    private final String myHost;
    private final String idpHost;
    private final Integer myPort;
    private final Integer idpPort;

    public AuthFilter(KeyProvider keyProvider,
                      @Value("${crud.server.host:localhost}") String myHost,
                      @Value("${idp.server.host:localhost}") String idpHost,
                      @Value("${crud.server.port:8080}") Integer myPort,
                      @Value("${idp.server.port:8081}") Integer idpPort) {
        this.keyProvider = keyProvider;
        this.myHost = myHost;
        this.idpHost = idpHost;
        this.myPort = myPort;
        this.idpPort = idpPort;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        String path = http(servletRequest).getRequestURI();

        if (isPublicPage(path)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }


        var cookies = http(servletRequest).getCookies() == null ?
                new Cookie[0] : http(servletRequest).getCookies();
        Optional<Cookie> c = Arrays.stream(cookies)
                .filter(x -> x.getName()
                        .equals("CORP-ID"))
                .findAny();


        if (c.isEmpty()) {
            if (path.startsWith("/api")) {
                respondWithError(servletResponse, "No credentials found.");
            } else {
                redirectToIdp(servletResponse);
            }
            return;
        }

        var cookie = c.get();

        JWTVerifier verifier = JWT.require(Algorithm.RSA256(keyProvider))
                .withIssuer("CORP LOGIN")
                .build();

        try {
            var decoded = verifier.verify(cookie.getValue());
            http(servletRequest).setAttribute("decodedJwt", decoded);
            filterChain.doFilter(servletRequest, servletResponse);

        } catch (JWTVerificationException ex) {
            respondWithError(servletResponse, "Token could not be verified.");
        }
    }

    private void respondWithError(ServletResponse servletResponse, String msg) throws IOException {
        http(servletResponse).setHeader("Content-Type",
                "text/plain");
        http(servletResponse).getWriter()
                .print(msg);
        http(servletResponse)
                .sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private void redirectToIdp(ServletResponse servletResponse) throws IOException {
        var redirectUrl = UriComponentsBuilder
                .fromUriString(
                        "http://{idpHost}:{idpPort}/authenticate?callback={callback}")
                .buildAndExpand(idpHost, idpPort,
                        "http://" + myHost + ":" + myPort + "/dashboard")
                .toUriString();
        http(servletResponse).sendRedirect(redirectUrl);
    }

    private boolean isPublicPage(String path) {
        return List.of("/index.html", "/dashboard.html", "/case.html", "/",
                        "/logout")
                .contains(path);
    }

    private HttpServletRequest http(ServletRequest request) {
        return (HttpServletRequest) request;
    }

    private HttpServletResponse http(ServletResponse response) {
        return (HttpServletResponse) response;
    }
}
