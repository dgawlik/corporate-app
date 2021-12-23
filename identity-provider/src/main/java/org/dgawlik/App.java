package org.dgawlik;

import org.dgawlik.repository.InMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableWebSecurity
public class App extends WebSecurityConfigurerAdapter {

    @Autowired
    InMemoryStore inMemoryStore;


    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests(authorize -> authorize
                        .mvcMatchers("/login*").permitAll()
                        .anyRequest().hasAuthority("AUTHENTICATED"))
                .authenticationManager(authentication -> {
                    var usr = authentication.getPrincipal() + "";
                    var usrDetails = inMemoryStore.getByEmail(usr);
                    var cred = authentication.getCredentials() + "";

                    if (!passwordEncoder().matches(cred, usrDetails.getPassword())) {
                        throw new BadCredentialsException("Incorrect Password");
                    }

                    return new UsernamePasswordAuthenticationToken(usr,
                            usrDetails.getPassword(),
                            AuthorityUtils.createAuthorityList("AUTHENTICATED"));
                })
                .formLogin()
                .loginPage("/login");
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
