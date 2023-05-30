package com.example.springsecurityapplication.repositories;

import com.example.springsecurityapplication.models.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Integer> {
    Optional<Person> findByLogin(String login);
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update Person p set p.role = 'ROLE_ADMIN' where p.id =:id")
    void setRoleAdmin(@Param("id") int id);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update Person p set p.role = 'ROLE_USER' where p.id =:id")
    void setRoleUser(@Param("id") int id);
}
