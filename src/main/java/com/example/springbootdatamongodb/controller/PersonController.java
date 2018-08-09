package com.example.springbootdatamongodb.controller;

import com.example.springbootdatamongodb.entity.Person;
import com.example.springbootdatamongodb.repository.PersonRepository;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.mapreduce.GroupBy;
import org.springframework.data.mongodb.core.mapreduce.GroupByResults;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author Administrator
 */
@RestController
public class PersonController {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @RequestMapping("/findByNameLike")
    public List<Person> findByNameLike(String name) {
        return personRepository.findByNameLike(name);
    }

    @RequestMapping("/find")
    public List<Person> find() {
        List<Person> ps = personRepository.find(true, "name");
        System.out.println(ps.size());
        return ps;
    }

    @RequestMapping("/map")
    public List<Person> map() {
        List<String> list = new ArrayList<>();
        List<Person> persons = new LinkedList<>();
        List<Person> all = mongoTemplate.findAll(Person.class);
        for (Person person : all) {
            if (!list.contains(person.getName())) {
                list.add(person.getName());
                persons.add(person);
            }
        }
        return persons;
    }

    @RequestMapping("/group2")
    public List<Map<String, Object>> group2() {
        GroupBy groupBy = GroupBy.key("name", "age").initialDocument("{}")
                .reduceFunction("function(doc, prev){}");

        GroupByResults<Document> results = mongoTemplate.group("person", groupBy, Document.class);
        Iterator<Document> it = results.iterator();
        List<Map<String, Object>> list = new LinkedList<>();
        while (it.hasNext()) {
            Document person = it.next();
            Map<String, Object> map = new HashMap<>();
            map.put("name", person.get("name"));
            map.put("age", person.get("age"));
            list.add(map);
        }
        return list;
    }

    @RequestMapping("/group")
    public List<Map<String, Object>> group() {
        GroupBy groupBy = GroupBy.key("name").initialDocument("{_id:[],total:0}")
                .reduceFunction("function(doc, prev){prev._id.push(doc._id);prev.total+=1}");

        GroupByResults<Document> results = mongoTemplate.group("person", groupBy, Document.class);
        Iterator<Document> it = results.iterator();
        List<Map<String, Object>> list = new LinkedList<>();
        while (it.hasNext()) {
            Document person = it.next();
            System.out.print("name:" + person.get("name"));
            System.out.print(" _id:" + person.get("_id"));
            System.out.println(" total:" + person.get("total"));

            Map<String, Object> map = new HashMap<>();
            map.put("name", person.get("name"));
            map.put("total", person.get("total"));
            map.put("_id", person.get("_id"));
            list.add(map);
        }
        return list;
    }

    @RequestMapping("/aggregate")
    public List<Map<String, Object>> aggregate() {
        List<Map<String, Object>> list = new ArrayList<>();
        TypedAggregation<Person> aggregation = Aggregation.newAggregation(Person.class,
                Aggregation.group("name", "age").count().as("count")
        );

        AggregationResults<Document> aggregate = mongoTemplate.aggregate(aggregation, Document.class);

        List<Document> permissionList = aggregate.getMappedResults();
        for (Document document : permissionList) {

            Map<String, Object> map = new HashMap<>();
            map.put("name", document.get("name"));
            map.put("count", document.get("count"));
            map.put("age", document.get("age"));
            list.add(map);
        }

        return list;
    }

    @RequestMapping("/distinct")
    public List<Integer> distinct(Integer age) {
        List<Integer> userHostIPList = new ArrayList<>();
        TypedAggregation<Person> aggregation = Aggregation.newAggregation(Person.class,
                Aggregation.group("name").count().as("count")
        );

        AggregationResults<Document> aggregate = mongoTemplate.aggregate(aggregation, Document.class);

        List<Document> permissionList = aggregate.getMappedResults();
        for (Document document : permissionList) {
            userHostIPList.add((Integer) document.get("count"));
        }

        return userHostIPList;
    }

    @RequestMapping("/save")
    public Person save(String name, Integer age) {
        Integer index = personRepository.findTopByOrderByIndexDesc().getIndex();

        Person p = new Person();
        p.setName(name);
        p.setAge(age);
        p.setIndex(index + 1);
        p.setCreateTime(new Date());
        p.setId(UUID.randomUUID().toString());
        return personRepository.save(p);
    }

    @RequestMapping("/get")
    public List<Person> get(String name) {
        List<Person> ps = personRepository.listByNameAge(name);
        for (Person p : ps) {
            System.out.println(p);
        }

        return personRepository.findByName(name);
    }

    @RequestMapping("/delete/{name}")
    public int delete(@PathVariable String name) {
        return personRepository.deleteByName(name);
    }

    @RequestMapping("/update")
    public Person update(String id) {
        //return personRepository.updateNameById("naff", id);
        return null;
    }
}
