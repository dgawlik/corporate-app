package org.dgawlik.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
public class ApiManager
        implements AuthenticationManager {

    private final String apiSecret;

    public Authentication authenticate(Authentication authentication) throws
                                                                      AuthenticationException {
        var usr = authentication.getPrincipal() + "";
        var cred = authentication.getCredentials() + "";


        if (!new BCryptPasswordEncoder().matches(apiSecret, cred)) {
            throw new BadCredentialsException("Incorrect API Secret");
        }

        return new UsernamePasswordAuthenticationToken(usr,
                apiSecret,
                AuthorityUtils.createAuthorityList("API_GRANTED"));
    }
}
