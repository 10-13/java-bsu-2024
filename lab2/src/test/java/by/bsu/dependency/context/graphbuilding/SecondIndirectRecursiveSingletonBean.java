package by.bsu.dependency.context.graphbuilding;

import by.bsu.dependency.annotation.Bean;
import by.bsu.dependency.annotation.BeanScope;
import by.bsu.dependency.annotation.Inject;

@Bean(scope = BeanScope.SINGLETON)
public class SecondIndirectRecursiveSingletonBean {
    @Inject
    FirstIndirectRecursiveSingletonBean val;
}
