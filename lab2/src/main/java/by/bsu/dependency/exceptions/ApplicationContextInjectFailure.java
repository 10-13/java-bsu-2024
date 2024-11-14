package by.bsu.dependency.exceptions;

import java.text.MessageFormat;

public class ApplicationContextInjectFailure extends ApplicationContextReflectionCallException {
    public ApplicationContextInjectFailure(String filedName, Throwable cause) {
        super(MessageFormat.format("Injecting field {0} failed.", filedName), cause);
    }
}