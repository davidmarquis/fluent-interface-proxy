package com.fluentinterface.domain;

import java.util.List;
import java.util.Queue;
import java.util.Set;

public class Person {
    private String name;
    private int age;
    private Person partner;
    private List<Person> friends;
    private Set<String> surnames;
    private Person[] parents;
    private int[] agesOfMarriages;
    private Queue queue;

    public Person() {
    }

    public Person(String name, int age, Person partner) {
        this.name = name;
        this.age = age;
        this.partner = partner;
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Person(Person partner, int age) {
        this.partner = partner;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Person getPartner() {
        return partner;
    }

    public void setPartner(Person partner) {
        this.partner = partner;
    }

    public List<Person> getFriends() {
        return friends;
    }

    public void setFriends(List<Person> friends) {
        this.friends = friends;
    }

    public Set<String> getSurnames() {
        return surnames;
    }

    public void setSurnames(Set<String> surnames) {
        this.surnames = surnames;
    }

    public Person[] getParents() {
        return parents;
    }

    public void setParents(Person[] parents) {
        this.parents = parents;
    }

    public int[] getAgesOfMarriages() {
        return agesOfMarriages;
    }

    public void setAgesOfMarriages(int[] agesOfMarriages) {
        this.agesOfMarriages = agesOfMarriages;
    }

    public Queue getQueue() {
        return queue;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }
}
