package com.fluentinterface.domain;

/**
 * YouTab Media 2011 Ltd.
 * Created by eladlaufer on 26/10/2015.
 */
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
