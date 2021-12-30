package org.dgawlik.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;

/**
 * Rendering is done by Vue.js - just return plain HTML.
 */
@Controller
public class SiteController {

    public static final String CACHE_CONTROL_HEADER = "Cache-Control";
    public static final String NO_CACHE_HEADER_VALUE = "no-cache, no-store, must-revalidate";
    private final String myHost;
    private final String idpHost;
    private final Integer myPort;
    private final Integer idpPort;

    public SiteController(@Value("${crud.server.host:localhost}") String myHost,
            @Value("${idp.server.host:localhost}") String idpHost,
            @Value("${crud.server.port:8080}") Integer myPort,
            @Value("${idp.server.port:8081}") Integer idpPort) {

        this.myHost = myHost;
        this.idpHost = idpHost;
        this.myPort = myPort;
        this.idpPort = idpPort;
    }

    @GetMapping(path = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index(HttpServletResponse resp) {

        resp.setHeader(CACHE_CONTROL_HEADER, NO_CACHE_HEADER_VALUE);
        return "index.html";
    }

    @GetMapping(path = "/dashboard", produces = MediaType.TEXT_HTML_VALUE)
    public String dashboard(HttpServletResponse resp) {

        resp.setHeader(CACHE_CONTROL_HEADER, NO_CACHE_HEADER_VALUE);
        return "dashboard.html";
    }

    @GetMapping(path = "/caseView", produces = MediaType.TEXT_HTML_VALUE)
    public String caseView(HttpServletResponse resp, @RequestParam String caseId) {

        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        return "case.html";
    }

    @GetMapping(path = "/logout", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> logoutRedirect(HttpServletResponse resp) {

        ResponseCookie deleteCookie = ResponseCookie
                .from("CORP-ID", "DELETE")
                .path("/")
                .maxAge(0)
                .build();

        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        var url = UriComponentsBuilder
                .fromUriString("http://{idpHost}:{idpPort}/logout?callback={cbk}")
                .buildAndExpand(idpHost, idpPort,
                        "http://" + myHost + ":" + myPort)
                .toUriString();

        return ResponseEntity
                .status(HttpStatus.TEMPORARY_REDIRECT)
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .header("Location", url)
                .build();
    }
}
