package by.bsu.dependency.context.building;

import by.bsu.dependency.annotation.Bean;
import by.bsu.dependency.annotation.BeanScope;
import by.bsu.dependency.annotation.Inject;
import by.bsu.dependency.exceptions.ApplicationContextRecursiveDependencyException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class DependencyGraphFactory {
    public static DependencyBuilder CreateDependencyBuilder(Class<?> type) {
        List<DependencyBuilder.Branch> brs = new ArrayList<>();
        int brsIndex = 1;

        Function<Class<?>, Stream<Field>> reciveDeps = (Class<?> l_type) ->
                Arrays.stream(l_type.getDeclaredFields())
                        .filter(fld -> fld.isAnnotationPresent(Inject.class));

        BiConsumer<Field, Integer> addBranch = (Field fld, Integer pIndex) -> brs.add(new DependencyBuilder.Branch(
                fld.getType(),
                fld,
                pIndex
        ));

        BiConsumer<Class<?>, Integer> addDeps = (Class<?> l_type, Integer ind) -> {
            reciveDeps.apply(l_type)
                    .forEach(el -> {
                        if (brs.get(ind).IsRecursive(brs, el.getType()))
                            throw new ApplicationContextRecursiveDependencyException(type, el.getType());
                        brs.add(new DependencyBuilder.Branch(
                                el.getType(),
                                el,
                                ind
                        ));
                    });
        };

        brs.add(new DependencyBuilder.Branch(
                type,
                null,
                -1
        ));

        addDeps.accept(brs.get(0).Type, 0);

        for (;brsIndex < brs.size(); brsIndex++) {
            var brt = brs.get(brsIndex).Type;
            if (brt.getDeclaredAnnotation(Bean.class).scope() == BeanScope.PROTOTYPE)
                addDeps.accept(brt, brsIndex);
        }

        return new DependencyBuilder(brs);
    }
}
