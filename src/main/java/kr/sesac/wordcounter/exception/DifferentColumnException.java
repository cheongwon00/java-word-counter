package kr.sesac.wordcounter.exception;

public class DifferentColumnException extends RuntimeException {
    public DifferentColumnException(String message) {
        super(message);
    }
}
