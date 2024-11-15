package by.bsu.dependency.context;

import by.bsu.dependency.annotation.Bean;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AutoScanApplicationContext extends AbstractApplicationContext {
    public AutoScanApplicationContext(String packageName) {
        findAllClassesUsingClassLoader(packageName).forEach(this::AddBean);
    }

    private List<? extends Class<?>> findAllClassesUsingClassLoader(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        if (stream == null)
            throw new RuntimeException("Resource not found");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .filter(Objects::nonNull)
                .filter(a->a.isAnnotationPresent(Bean.class))
                .collect(Collectors.toList());
    }

    private Class<?> getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}