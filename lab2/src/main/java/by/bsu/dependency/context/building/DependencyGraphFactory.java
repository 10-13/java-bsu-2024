package by.bsu.dependency.context.building;

import by.bsu.dependency.annotation.Bean;
import by.bsu.dependency.annotation.BeanScope;
import by.bsu.dependency.annotation.Inject;
import by.bsu.dependency.exceptions.ApplicationContextRecursiveDependencyException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class DependencyGraphFactory {
    public static DependencyBuilder CreateDependencyBuilder(Class<?> type) {
        List<DependencyBuilder.Branch> branches = new ArrayList<>();
        int branchListIndex = 1;

        BiConsumer<Class<?>, Integer> addDeps = (Class<?> l_type, Integer ind) -> {
            Arrays.stream(l_type.getDeclaredFields())
                    .filter(fld -> fld.isAnnotationPresent(Inject.class))
                    .forEach(el -> {
                        if (branches.get(ind).IsRecursive(branches, el.getType()))
                            throw new ApplicationContextRecursiveDependencyException(type, el.getType());
                        branches.add(new DependencyBuilder.Branch(el.getType(),el,ind));
                    });
        };

        branches.add(new DependencyBuilder.Branch(type,null,-1));
        addDeps.accept(branches.get(0).Type, 0);

        for (;branchListIndex < branches.size(); branchListIndex++) {
            var targetBranch = branches.get(branchListIndex).Type;
            if (targetBranch.getDeclaredAnnotation(Bean.class).scope() == BeanScope.PROTOTYPE)
                addDeps.accept(targetBranch, branchListIndex);
        }

        return new DependencyBuilder(branches);
    }
}
