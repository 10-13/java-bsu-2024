package by.bsu.dependency.exceptions;

public class ApplicationContextNotStartedException extends ApplicationContextException {
    public ApplicationContextNotStartedException() {
        super("Application context is not started already.");
    }
}