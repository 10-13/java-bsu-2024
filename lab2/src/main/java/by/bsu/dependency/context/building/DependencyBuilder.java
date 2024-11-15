package by.bsu.dependency.context.building;

import by.bsu.dependency.annotation.Bean;
import by.bsu.dependency.annotation.BeanScope;
import by.bsu.dependency.exceptions.ApplicationContextInjectFailure;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DependencyBuilder {
    private final List<Branch> Branches_;
    private final List<Object> Instances_;

    DependencyBuilder(List<Branch> branches) {
        Branches_ = branches;
        Instances_ = branches.stream().map(a->null).collect(Collectors.toList());
    }

    public Stream<Object> DependenciesStream() {
        return Instances_.stream()
                .skip(1)
                .filter(Objects::nonNull)
                .distinct();
    }
    public Stream<Object> PrototypeDependenciesStream() {
        return Instances_.stream()
                .skip(1)
                .filter(Objects::nonNull)
                .filter(obj -> obj.getClass().getDeclaredAnnotation(Bean.class).scope() == BeanScope.PROTOTYPE);
    }

    public void InstantiateDependencies(DependencyProvider provider) {
        for (int i = 0; i < Branches_.size(); i++) {
            Instances_.set(i, provider.GetDependencyUninitializedInstance(Branches_.get(i).Type));
        }
    }
    public void BuildGraph() {
        for (int i = 1; i < Instances_.size(); i++) {
            var br = Branches_.get(i);

            br.Fld.setAccessible(true);
            try {
                br.Fld.set(Instances_.get(br.Parent), Instances_.get(i));
            } catch (IllegalAccessException e) {
                throw new ApplicationContextInjectFailure(br.Fld.getName(), e);
            }
        }
    }
    public Object GetObject() {
        return Instances_.get(0);
    }

    static class Branch {
        Class<?> Type;
        Field Fld;
        int Parent;

        Branch(Class<?> Type, Field Fld, int Parent) {
            this.Type = Type;
            this.Fld = Fld;
            this.Parent = Parent;
        }

        boolean IsRecursive(List<Branch> data, Class<?> type) {
            return Type == type || (Parent >= 0 && data.get(Parent).IsRecursive(data, type));
        }
    }
}

