package kr.sesac.wordcounter.exception;

public class MissingTargetHeaderException extends RuntimeException {
    public MissingTargetHeaderException(String message) {
        super(message);
    }
}
