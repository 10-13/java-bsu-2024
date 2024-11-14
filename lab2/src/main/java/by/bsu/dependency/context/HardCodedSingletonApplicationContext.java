package by.bsu.dependency.context;

public class HardCodedSingletonApplicationContext extends AbstractApplicationContext {
    public HardCodedSingletonApplicationContext(Class<?>... beanClasses) {
        for (var type : beanClasses)
            AddBean(type);
    }
}