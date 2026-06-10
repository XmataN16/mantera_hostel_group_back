// src/main/java/ru/mantera/hostel/exception/ResourceNotFoundException.java
package ru.mantera.hostel.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}