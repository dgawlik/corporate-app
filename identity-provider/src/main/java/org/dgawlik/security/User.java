package org.dgawlik.security;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    String email;
    String firstName;
    String lastName;
    String password;
}
