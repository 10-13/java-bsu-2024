package by.bsu.dependency.exceptions;

import java.text.MessageFormat;

public class ApplicationContextPostConstructFailure extends ApplicationContextReflectionCallException {
    public ApplicationContextPostConstructFailure(String methodName, Throwable cause) {
        super(MessageFormat.format("Running post construct {0} failed.", methodName), cause);
    }
}