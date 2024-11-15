package by.bsu.dependency.context.graphbuilding;

import by.bsu.dependency.annotation.Bean;
import by.bsu.dependency.annotation.BeanScope;
import by.bsu.dependency.annotation.Inject;

@Bean(scope = BeanScope.PROTOTYPE)
public class ComplexPrototypeBean {
    @Inject
    public SimplePrototypeBean a;
    @Inject
    public SimplePrototypeBean b;
    @Inject
    public SimplePrototypeBean c;
}
