package by.bsu.dependency.context.graphbuilding;

import by.bsu.dependency.context.ApplicationContext;
import by.bsu.dependency.context.AutoScanApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class GraphBuildingTests {

    private ApplicationContext applicationContext;

    @BeforeEach
    void init() {
        applicationContext = new AutoScanApplicationContext(GraphBuildingTests.class.getPackageName());
    }

    @Test
    void testIsPrototypesBuilding() {
        applicationContext.start();

        var bean = applicationContext.getBean(ComplexPrototypeBean.class);

        assertThat(Objects.nonNull(bean.a)).isTrue();
        assertThat(Objects.nonNull(bean.b)).isTrue();
        assertThat(Objects.nonNull(bean.c)).isTrue();
    }

    // TODO: Tests to be added:
    //  - Recursion:
    //    - Recursion prototypes [ERR:getBean]
    //    - Indirect recursion prototypes (2 variants) [ERR:getBean]
    //    - Recursion singletons [SUC]
    //    - Indirect recursion prototypes inside singleton [ERR:start]
    //  - Order:
    //    - Ordered post constructors
    //    - Post constructors called
}
