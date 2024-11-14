### Построение бинов

- Построение дерева зависимостей [DepFactory -> DependencyBuilder]
- Инициализация зависимостей [DependencyBuilder.initialzie(IDepsSource)]
- Предобработка зависимостей [DependencyBuilder.depsStream()]
- Сборка объекта (инъекция зависимостей) [DependencyBuilder.buildInstance()]
- Постобрапботка зависимостей [DependencyBuilder.depsStream()]
- Получение объекта [DependencyBuilder.getObject()]