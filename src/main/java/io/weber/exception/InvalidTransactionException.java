package io.weber.exception;

public class InvalidTransactionException extends Exception {
    public InvalidTransactionException(String message){
        super(message);
    }
}
