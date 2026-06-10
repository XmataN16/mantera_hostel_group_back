// src/main/java/ru/mantera/hostel/exception/ConflictException.java
package ru.mantera.hostel.exception;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}