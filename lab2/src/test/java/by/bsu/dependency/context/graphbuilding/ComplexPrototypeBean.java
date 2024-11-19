package by.bsu.dependency.context.graphbuilding;

import by.bsu.dependency.annotation.Bean;
import by.bsu.dependency.annotation.BeanScope;
import by.bsu.dependency.annotation.Inject;
import by.bsu.dependency.annotation.PostConstruct;

@Bean(scope = BeanScope.PROTOTYPE)
public class ComplexPrototypeBean {
    @Inject
    public SimplePrototypeBean a;
    @Inject
    public SimplePrototypeBean b;
    @Inject
    public SimplePrototypeBean c;

    @PostConstruct
    void check() {
        if (a == null || b == null || c == null)
            throw new RuntimeException("Invalid injects on post construct");
    }
}
