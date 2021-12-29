package org.dgawlik.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class ApiSecretFilter
        implements Filter {


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
                                                                                              IOException,
                                                                                              ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        var apiToken = httpRequest.getHeader("Authorization");
        if (apiToken != null && apiToken.contains("Bearer")) {
            apiToken = apiToken.replace("Bearer", "")
                               .trim();

            SecurityContextHolder.getContext()
                                 .setAuthentication(new UsernamePasswordAuthenticationToken("__api", apiToken));
        }
        chain.doFilter(request, response);
    }
}
