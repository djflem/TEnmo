package com.techelevator.tenmo.exception;

public class TransferExceptions {

    public static class TransferListNotFoundException extends RuntimeException {
        public TransferListNotFoundException(String message) {
            super(message);
        }
    }
    public static class TransferNotFoundException extends RuntimeException {
        public TransferNotFoundException(String message) {
            super(message);
        }
    }

    public static class TransferCreationException extends RuntimeException {
        public TransferCreationException(String message) {
            super(message);
        }
    }

    public static class TransferUpdateException extends RuntimeException {
        public TransferUpdateException(String message) {
            super(message);
        }
    }
}
