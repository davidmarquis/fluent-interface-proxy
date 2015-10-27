package com.fluentinterface.domain;


public class TypedID<T> {

    private String id;

    public TypedID() {
    }

    public TypedID(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
