package com.techelevator.tenmo.exception;

public class AccountExceptions {

    public static class AccountListNotFoundException extends RuntimeException {
        public AccountListNotFoundException(String message) {
            super(message);
        }
    }

    public static class AccountNotFoundException extends RuntimeException {
        public AccountNotFoundException(String message) {
            super(message);
        }
    }


    public static class AccountUpdateException extends RuntimeException {
        public AccountUpdateException(String message) {
            super(message);
        }
    }
}