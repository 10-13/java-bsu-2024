package by.bsu.dependency.exceptions;

import java.text.MessageFormat;

public class ApplicationContextDoNotContainsSuchBeanDefinitionException extends ApplicationContextException {
    public ApplicationContextDoNotContainsSuchBeanDefinitionException(String beanName) {
        super(MessageFormat.format("No bean with name \"{0}\" found.", beanName));
    }
}