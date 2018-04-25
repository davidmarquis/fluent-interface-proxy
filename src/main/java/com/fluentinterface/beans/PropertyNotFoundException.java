package com.fluentinterface.beans;

public class PropertyNotFoundException extends RuntimeException {
    PropertyNotFoundException(String message) {
        super(message);
    }
}
