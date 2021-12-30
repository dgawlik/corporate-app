package org.dgawlik.domain.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Interesting solution worth noting here is how
 * the approval chain is created.
 * <p>
 * Person A initates case and creates case c1.
 * Person B sees c1, approves it by creating c2.
 * c1 is nested in c2, c2 replaces c1 with same id in mongo.
 * Person C sees c2 ...
 * <p>
 * See field thread.
 */
@Document
@Getter
@Setter
@Builder
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Case {

    @Id
    private String id;

    private Person initiatingPerson;

    private Person subjectPerson;

    private String justification;

    private Instant timestamp;

    private Boolean approved;

    private Boolean done;

    private Action action;

    private Case thread;
}
