package com.example.springbootdatamongodb.repository;

import com.example.springbootdatamongodb.entity.Person;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PersonRepository extends MongoRepository<Person, String> {

    List<Person> findByName(String name);

    @Query(value = "{'name': ?0}", fields = "{'name': 1}")
    List<Person> listByNameAge(String name);

    Person findTopByOrderByIndexDesc();

    @Query("{'id': ?#{ [0] ? {$exists :true} : [1] }}")
    List<Person> find(boolean param0, String param1);

    int deleteByName(String name);

    Person updateNameById(String name, String id);
}
