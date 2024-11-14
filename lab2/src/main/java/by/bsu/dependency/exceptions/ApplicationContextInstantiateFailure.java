package by.bsu.dependency.exceptions;

import java.text.MessageFormat;

public class ApplicationContextInstantiateFailure extends ApplicationContextReflectionCallException {
    public ApplicationContextInstantiateFailure(String typeName, Throwable cause) {
        super(MessageFormat.format("Instantiating type {0} failed.", typeName), cause);
    }
}