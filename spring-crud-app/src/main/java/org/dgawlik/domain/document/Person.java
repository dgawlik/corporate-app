package org.dgawlik.domain.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


/**
 * By navigating parentId and childrenIds we
 * can simulate hierarchy tree.
 */
@Document
@Builder
@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class Person {

    @Id
    private String id;

    private String parentId;

    private List<String> childrenIds;

    private String firstName;

    private String lastName;

    private Integer age;

    private Role role;

    private Integer appraisals;

    private Double salary;

    private List<String> caseIds;
}
