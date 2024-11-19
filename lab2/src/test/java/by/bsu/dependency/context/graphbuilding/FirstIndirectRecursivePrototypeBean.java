package by.bsu.dependency.context.graphbuilding;

import by.bsu.dependency.annotation.Bean;
import by.bsu.dependency.annotation.BeanScope;
import by.bsu.dependency.annotation.Inject;

@Bean(scope = BeanScope.PROTOTYPE)
public class FirstIndirectRecursivePrototypeBean {
    @Inject
    SecondIndirectRecursivePrototypeBean val;
}
