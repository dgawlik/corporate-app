package org.dgawlik.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dgawlik.domain.document.Role;

/**
 * We want to have limited view of Person in aggregations
 * (for ex. salary)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LimitedPersonView {

    String id;
    String firstName;
    String lastName;
    Role role;
}
