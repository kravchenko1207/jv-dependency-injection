package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

public class Injector {
    private static final Injector injector = new Injector();
    private final Set<Class<?>> creating = new HashSet<>();
    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!interfaceClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't find the annotation!");
        }
        if (creating.contains(interfaceClazz)) {
            throw new RuntimeException("Cyclic dependency detected: " + interfaceClazz);
        }
        creating.add(interfaceClazz);
        Object instance;
        try {
            Constructor<?> constructor = interfaceClazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            instance = constructor.newInstance();
            for (Field field : interfaceClazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Inject.class)) continue;
                Class<?> fieldType = field.getType();
                Class<?> impl = fieldType;
                Object dependency = getInstance(impl);
                field.setAccessible(true);
                field.set(instance, dependency);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            creating.remove(interfaceClazz);
        }
        return instance;
    }
}
