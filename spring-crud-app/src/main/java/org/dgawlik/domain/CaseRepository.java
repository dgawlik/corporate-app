package org.dgawlik.domain;

import org.dgawlik.domain.document.Case;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CaseRepository extends MongoRepository<Case, String> {
    <T> List<T> findAllProjectedBy(Class<T> klass);

    <T> Optional<T> findById(String id, Class<T> klass);
}
