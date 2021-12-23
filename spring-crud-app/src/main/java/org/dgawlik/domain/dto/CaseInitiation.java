package org.dgawlik.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dgawlik.domain.document.Action;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaseInitiation {
    @NotNull
    String onBehalfId;
    @NotNull
    LimitedPersonView subject;
    @NotNull
    Action action;
    @NotNull
    String justification;
}
