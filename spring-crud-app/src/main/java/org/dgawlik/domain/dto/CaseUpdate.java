package org.dgawlik.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaseUpdate {
    @NotNull
    String caseId;
    @NotNull
    String onBehalfId;
    @NotNull
    Boolean approve;
    @NotNull
    String justification;
}
