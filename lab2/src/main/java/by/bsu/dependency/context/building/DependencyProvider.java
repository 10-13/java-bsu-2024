package by.bsu.dependency.context.building;

public interface DependencyProvider {
    Object GetDependencyUninitializedInstance(Class<?> type);
}
