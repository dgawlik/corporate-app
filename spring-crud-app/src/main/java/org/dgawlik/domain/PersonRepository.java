package org.dgawlik.domain;

import org.dgawlik.domain.document.Person;
import org.dgawlik.domain.document.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends MongoRepository<Person, String> {

    Optional<Person> findByFirstNameAndLastName(String firstName, String lastName);

    List<Person> findByParentId(String parentId);

    List<Person> findByRole(Role role);

    <T> List<T> findAllProjectedBy(Class<T> klass);
}
