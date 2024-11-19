package by.bsu.dependency.context;

import by.bsu.dependency.annotation.Bean;
import by.bsu.dependency.annotation.BeanScope;
import by.bsu.dependency.annotation.PostConstruct;
import by.bsu.dependency.context.building.DependencyBuilder;
import by.bsu.dependency.context.building.DependencyGraphFactory;
import by.bsu.dependency.context.building.DependencyProvider;
import by.bsu.dependency.exceptions.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;


public abstract class AbstractApplicationContext implements ApplicationContext {
    private final HashMap<String, Class<?>> NamesToTypes_ = new HashMap<>();
    private final HashMap<Class<?>, BeanScope> TypeScopes_ = new HashMap<>();
    private final HashMap<Class<?>, Object> InstantiatedBeans_ = new HashMap<>();

    private ContextStatus Status_ = ContextStatus.NOT_STARTED;

    @Override
    public boolean isRunning() {
        return Status_ == ContextStatus.STARTED;
    }
    @Override
    public boolean containsBean(String name) {
        CheckContextRunning_();
        return NamesToTypes_.containsKey(name);
    }
    @Override
    public Object getBean(String name) {
        return getBean(GetBeanType(name));
    }

    @Override
    public boolean isSingleton(String name) {
        CheckBeanCorrectness_(name);
        return GetBeanScope(GetBeanType(name)) == BeanScope.SINGLETON;
    }
    @Override
    public boolean isPrototype(String name) {
        CheckBeanCorrectness_(name);
        return GetBeanScope(GetBeanType(name)) == BeanScope.PROTOTYPE;
    }

    protected void AddBean(Class<?> type) {
        var bean = type.getDeclaredAnnotation(Bean.class);
        NamesToTypes_.put(
                bean.name().isEmpty() ? BeanHelper.CombineName(type) : bean.name(),
                type);
        TypeScopes_.put(type, bean.scope());
    }
    @Override
    public void start() {
        List<DependencyBuilder> builders = new ArrayList<>();

        this.TypeScopes_.entrySet().stream()
                .filter(entry -> entry.getValue() == BeanScope.SINGLETON)
                .forEach(type -> builders.add(DependencyGraphFactory.CreateDependencyBuilder(type.getKey())));

        builders.forEach(builder -> builder.InstantiateDependencies(CreateDependencyProvider()));
        builders.forEach(DependencyBuilder::BuildGraph);
        builders.forEach(builder -> builder.PrototypeDependenciesStream().forEach(this::PostConstruct_));
        builders.forEach(builder -> PostConstruct_(builder.GetObject()));

        Status_ = ContextStatus.STARTED;
    }
    @Override
    public <T> T getBean(Class<T> clazz) {
        CheckContextRunning_();
        CheckBeanCorrectness_(clazz);
        if (TypeScopes_.get(clazz) == BeanScope.SINGLETON)
            return (T)InstantiatedBeans_.get(clazz);

        var bld = DependencyGraphFactory.CreateDependencyBuilder(clazz);
        bld.InstantiateDependencies(CreateDependencyProvider());
        bld.BuildGraph();
        bld.PrototypeDependenciesStream().forEach(this::PostConstruct_);
        return (T)PostConstruct_(bld.GetObject());
    }

    private Class<?> GetBeanType(String name) {
        return NamesToTypes_.get(name);
    }
    private BeanScope GetBeanScope(Class<?> type) {
        return TypeScopes_.get(type);
    }

    private void CheckBeanCorrectness_(Class<?> type) {
        if (!TypeScopes_.containsKey(type))
            throw new ApplicationContextDoNotContainsSuchBeanDefinitionException(BeanHelper.CombineName(type));
    }
    private void CheckBeanCorrectness_(String name) {
        if (!NamesToTypes_.containsKey(name))
            throw new ApplicationContextDoNotContainsSuchBeanDefinitionException(name);
    }
    private void CheckContextRunning_() {
        if (!isRunning())
            throw new ApplicationContextNotStartedException();
    }

    private Object PostConstruct_(Object obj) {
        Arrays.stream(obj.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(PostConstruct.class))
                .forEach(method -> {
                    method.setAccessible(true);
                    try {
                        method.invoke(obj);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new ApplicationContextPostConstructFailure(method.getName(), e);
                    }
                });
        return obj;
    }

    private DependencyProvider CreateDependencyProvider() {
        return new DependencyProvider() {
            private final InstanceFabric Fabric_ = new InstanceFabric();

            @Override
            public Object GetDependencyUninitializedInstance(Class<?> type) {
                return Fabric_.InstantiateBean(type);
            }
        };
    }

    private enum ContextStatus {
        NOT_STARTED,
        STARTED
    }

    private static class BeanHelper {
        public static String CombineName(Class<?> type) {
            return Character.toLowerCase(type.getSimpleName().charAt(0)) + type.getSimpleName().substring(1);
        }
    }

    private class InstanceFabric {
        public Object InstantiateType(Class<?> type) {
            try {
                var ctor = type.getDeclaredConstructor();
                ctor.setAccessible(true);
                return ctor.newInstance();
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new ApplicationContextInstantiateFailure(type.getName(), e);
            }
        }
        public Object InstantiateBean(Class<?> type, BeanScope scope) {
            if (scope == BeanScope.PROTOTYPE)
                return InstantiateType(type);

            if (!InstantiatedBeans_.containsKey(type) || Objects.isNull(InstantiatedBeans_.get(type)))
                InstantiatedBeans_.put(type, InstantiateType(type));

            return InstantiatedBeans_.get(type);
        }
        public Object InstantiateBean(Class<?> type) {
            if (!TypeScopes_.containsKey(type))
                throw new ApplicationContextDoNotContainsSuchBeanDefinitionException(type.getName());

            return  InstantiateBean(type, TypeScopes_.get(type));
        }
    }
}