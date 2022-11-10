package io.weber.exception;

public class OverdraftException extends RuntimeException{
    public OverdraftException(String message){
        super(message);
    }
}
