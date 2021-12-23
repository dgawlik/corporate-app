package org.dgawlik.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LimitedCaseView {
    String id;
    Boolean approved;
    Boolean done;
    LimitedPersonView initiatingPerson;
    LimitedPersonView subjectPerson;
    LimitedCaseView thread;
    String justification;
}
