package cn.edu.tongji.swim.lib;

public class CustomExceptions {

    public static class JoinFailedException extends RuntimeException {
        public JoinFailedException(String message) {
            super(message);
        }
    }

    public static class InvalidStateException extends RuntimeException {
        public InvalidStateException(String message) {
            super(message);
        }
    }

    public static class ListenFailedException extends RuntimeException {
        public ListenFailedException(String message) {
            super(message);
        }
    }
}