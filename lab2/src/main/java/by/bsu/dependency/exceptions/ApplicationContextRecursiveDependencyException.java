package by.bsu.dependency.exceptions;

public class ApplicationContextRecursiveDependencyException extends ApplicationContextException {
    public ApplicationContextRecursiveDependencyException(Class<?> checkedType, Class<?> recursedType)
    {
        super("Find recursive dependency during checking: " + checkedType.getSimpleName() +
                "(Occurred by: " + recursedType.getSimpleName() + ")");
    }
}