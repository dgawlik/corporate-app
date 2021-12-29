package org.dgawlik.security;

import lombok.RequiredArgsConstructor;
import org.dgawlik.repository.InMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
public class LoginManager
        implements AuthenticationManager {


    private final InMemoryStore inMemoryStore;

    public Authentication authenticate(Authentication authentication) throws
                                                                      AuthenticationException {
        var usr = authentication.getPrincipal() + "";
        var usrDetails = inMemoryStore.getByEmail(usr);
        var cred = authentication.getCredentials() + "";

        if (!new BCryptPasswordEncoder().matches(cred, usrDetails.getPassword())) {
            throw new BadCredentialsException("Incorrect Password");
        }

        return new UsernamePasswordAuthenticationToken(usr,
                usrDetails.getPassword(),
                AuthorityUtils.createAuthorityList("AUTHENTICATED"));
    }
}
