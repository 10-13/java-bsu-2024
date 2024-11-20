package by.bsu.dependency.context.graphbuilding;

import by.bsu.dependency.context.ApplicationContext;
import by.bsu.dependency.context.AutoScanApplicationContext;
import by.bsu.dependency.context.HardCodedSingletonApplicationContext;
import by.bsu.dependency.exceptions.ApplicationContextRecursiveDependencyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GraphBuildingTests {
    @Test
    void testIsPrototypesBuilding() {
        ApplicationContext applicationContext = new HardCodedSingletonApplicationContext(ComplexPrototypeBean.class, SimplePrototypeBean.class);

        applicationContext.start();

        var bean = applicationContext.getBean(ComplexPrototypeBean.class);

        assertThat(Objects.nonNull(bean.a)).isTrue();
        assertThat(Objects.nonNull(bean.b)).isTrue();
        assertThat(Objects.nonNull(bean.c)).isTrue();
    }

    @Test
    void testPrototypesSimpleRecursion() {
        ApplicationContext applicationContext = new HardCodedSingletonApplicationContext(
                SimpleRecursivePrototypeBean.class);

        applicationContext.start();

        assertThrows(
                ApplicationContextRecursiveDependencyException.class,
                () -> applicationContext.getBean(SimpleRecursivePrototypeBean.class)
        );
    }

    @Test
    void testPrototypesIndirectRecursion() {
        ApplicationContext applicationContext = new HardCodedSingletonApplicationContext(
                FirstIndirectRecursivePrototypeBean.class, SecondIndirectRecursivePrototypeBean.class, ThirdIndirectRecursivePrototypeBean.class);

        applicationContext.start();

        assertThrows(
                ApplicationContextRecursiveDependencyException.class,
                () -> applicationContext.getBean(FirstIndirectRecursivePrototypeBean.class)
        );
        assertThrows(
                ApplicationContextRecursiveDependencyException.class,
                () -> applicationContext.getBean(SecondIndirectRecursivePrototypeBean.class)
        );
        assertThrows(
                ApplicationContextRecursiveDependencyException.class,
                () -> applicationContext.getBean(ThirdIndirectRecursivePrototypeBean.class)
        );
    }

    @Test
    void testSingletonSimpleRecursion() {
        ApplicationContext applicationContext = new HardCodedSingletonApplicationContext(SimpleRecursiveSingletonBean.class);

        applicationContext.start();
    }

    @Test
    void testSingletonIndirectRecursion() {
        ApplicationContext applicationContext = new HardCodedSingletonApplicationContext(FirstIndirectRecursiveSingletonBean.class, SecondIndirectRecursiveSingletonBean.class);

        applicationContext.start();
    }

    @Test
    void testPrototypeIndirectRecursionInsideSingleton() {
        ApplicationContext applicationContext = new HardCodedSingletonApplicationContext(SimpleRecursivePrototypeInsideSingletonBean.class, SimpleRecursivePrototypeBean.class);

        assertThrows(
                ApplicationContextRecursiveDependencyException.class,
                applicationContext::start
        );
    }
}
