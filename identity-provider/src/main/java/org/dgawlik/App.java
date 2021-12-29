package org.dgawlik;

import org.dgawlik.repository.InMemoryStore;
import org.dgawlik.security.ApiManager;
import org.dgawlik.security.ApiSecretFilter;
import org.dgawlik.security.LoginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@SpringBootApplication
@EnableWebSecurity
public class App {

    @Autowired
    InMemoryStore inMemoryStore;

    @Value("${server.api.secret}")
    String apiSecret;


    @Configuration
    @Order(1)
    class Chain1Config
            extends WebSecurityConfigurerAdapter {
        protected void configure(HttpSecurity http) throws
                                                    Exception {
            http
                    .mvcMatcher("/api/user/*")
                    .authenticationManager(new ApiManager(apiSecret))
                    .addFilterBefore(new ApiSecretFilter(), UsernamePasswordAuthenticationFilter.class)
                    .authorizeRequests(authorize -> authorize.anyRequest()
                                                             .hasAuthority("API_GRANTED"))
                    .csrf().disable();
        }
    }

    @Configuration
    @Order(2)
    class Chain2Config
            extends WebSecurityConfigurerAdapter {
        protected void configure(HttpSecurity http) throws
                                                    Exception {
            http
                    .mvcMatcher("/**")
                    .authenticationManager(new LoginManager(inMemoryStore))
                    .authorizeRequests(authorize -> authorize
                            .mvcMatchers("/login*")
                            .permitAll()
                            .anyRequest()
                            .hasAuthority("AUTHENTICATED"))
                    .formLogin()
                    .loginPage("/login");
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
