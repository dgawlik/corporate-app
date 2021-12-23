package org.dgawlik.domain.dto;

import lombok.Value;
import org.dgawlik.domain.document.Action;
import org.dgawlik.domain.document.Person;

import java.util.List;

/**
 * Update batch for rendering employees dashboard.
 */
@Value
public class Fill {
    Person me;
    List<LimitedPersonView> colleagues;
    List<LimitedCaseView> myCases;
    List<Action> actions;
}
