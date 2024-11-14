package by.bsu.dependency.example;

import by.bsu.dependency.context.ApplicationContext;
import by.bsu.dependency.context.HardCodedSingletonApplicationContext;
import by.bsu.dependency.context.building.DependencyGraphFactory;

public class Main {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new HardCodedSingletonApplicationContext(
                FirstBean.class, OtherBean.class
        );
        applicationContext.start();

        FirstBean firstBean = (FirstBean) applicationContext.getBean("firstBean");
        OtherBean otherBean = (OtherBean) applicationContext.getBean("otherBean");

        firstBean.doSomething();
        otherBean.doSomething();

        var a = DependencyGraphFactory.CreateDependencyBuilder(OtherBean.class);

        // Метод падает, так как в классе HardCodedSingletonApplicationContext не реализовано внедрение зависимостей
        // otherBean.doSomethingWithFirst();
    }
}
