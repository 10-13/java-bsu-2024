package by.bsu.dependency.context;

import by.bsu.dependency.annotation.Bean;
import by.bsu.dependency.annotation.BeanScope;
import by.bsu.dependency.annotation.Inject;
import by.bsu.dependency.annotation.PostConstruct;
import by.bsu.dependency.context.building.DependencyProvider;
import by.bsu.dependency.exceptions.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/*
Start pattern:
- instantiate static members
- create deps builders for static members
- inject all builder
- run post constructors for static members
- run post constructors for created prototypes
 */

public abstract class AbstractApplicationContext implements ApplicationContext {
    @Override
    public void start() {
        this.ClassedBeans_.forEach((type, scope) -> {
            if (scope == BeanScope.SINGLETON)
                this.InstantiatedBeans_.put(type, Instantiate_(type));
        });
        this.InstantiatedBeans_.forEach((idea, obj) -> PostConstruct_(Inject_(obj)));
        Status_ = ContextStatus.STARTED;
    }

    @Override
    public boolean isRunning() {
        return Status_ == ContextStatus.STARTED;
    }

    @Override
    public boolean containsBean(String name) {
        AssertRunning_();
        return BeanTemplates_.containsKey(name);
    }

    @Override
    public Object getBean(String name) {
        AssertRunning_();
        CheckBeanCorrectness_(name);
        return GetBean(GetBeanType(name));
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        AssertRunning_();
        CheckBeanCorrectness_(clazz);
        return (T)GetBean(clazz);
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

    private final HashMap<String, Class<?>> BeanTemplates_ = new HashMap<>();
    private final HashMap<Class<?>, BeanScope> ClassedBeans_ = new HashMap<>();
    private final HashMap<Class<?>, Object> InstantiatedBeans_ = new HashMap<>();

    private ContextStatus Status_ = ContextStatus.NOT_STARTED;

    public static class BeanHelper {
        public static String CombineName(Class<?> type) {
            return Character.toLowerCase(type.getSimpleName().charAt(0)) + type.getSimpleName().substring(1);
        }
    }

    protected void AddBean(Class<?> type) {
        var bean = type.getDeclaredAnnotation(Bean.class);
        BeanTemplates_.put(
                bean.name().isEmpty() ? BeanHelper.CombineName(type) : bean.name(),
                type);
        ClassedBeans_.put(type, bean.scope());
    }

    private Class<?> GetBeanType(String name) {
        return BeanTemplates_.get(name);
    }
    private BeanScope GetBeanScope(Class<?> type) { return ClassedBeans_.get(type); }

    private void CheckBeanCorrectness_(Class<?> type) {
        if (!ClassedBeans_.containsKey(type))
            throw new ApplicationContextDoNotContainsSuchBeanDefinitionException(BeanHelper.CombineName(type));
    }

    private void CheckBeanCorrectness_(String name) {
        if (!BeanTemplates_.containsKey(name))
            throw new ApplicationContextDoNotContainsSuchBeanDefinitionException(name);
    }

    private Object GetBean(Class<?> type) {
        if (GetBeanScope(type) == BeanScope.SINGLETON)
            return InstantiatedBeans_.get(type);
        return PostConstruct_(Inject_(Instantiate_(type)));
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

    private Object Inject_(Object obj) {
        Arrays.stream(obj.getClass().getDeclaredFields())
                .filter(fld->fld.isAnnotationPresent(Inject.class))
                .forEach(fld -> {
                    fld.setAccessible(true);
                    CheckBeanCorrectness_(fld.getType());
                    try {
                        fld.set(obj, GetBean(fld.getType()));
                    } catch (IllegalAccessException ex) {
                        throw new ApplicationContextInjectFailure(fld.getName(), ex);
                    }
                });
        return obj;
    }

    private Object Instantiate_(Class<?> type) {
        try {
            return type.getDeclaredConstructor(new Class[]{}).newInstance();
        } catch (NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException e) {
            throw new ApplicationContextInstantiateFailure(type.getName(), e);
        }
    }

    class InstanceFabric {
        public static Object InstantiateType(Class<?> type) {
            try {
                var ctor = type.getConstructor();
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
            if (!ClassedBeans_.containsKey(type))
                throw new ApplicationContextDoNotContainsSuchBeanDefinitionException(type.getName());

            return  InstantiateBean(type, ClassedBeans_.get(type));
        }
    }

    protected enum ContextStatus {
        NOT_STARTED,
        STARTED
    }

    private DependencyProvider CreateProvider() {
        return new DependencyProvider() {
            private final InstanceFabric Fabric_ = new InstanceFabric();

            @Override
            public Object GetDependencyUninitializedInstance(Class<?> type) {
                return Fabric_.InstantiateBean(type);
            }
        };
    }


    private void AssertRunning_() {
        if (!isRunning())
            throw new ApplicationContextNotStartedException();
    }
}