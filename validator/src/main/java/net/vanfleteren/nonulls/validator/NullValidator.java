package net.vanfleteren.nonulls.validator;

import net.vanfleteren.nonulls.validator.spi.RecursiveValidator;
import net.vanfleteren.nonulls.validator.spi.TypeValidator;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.*;

/**
 * Utility class for validating that an object graph contains no null values.
 * This includes recursively checking inside collections, arrays, optionals, pojos and records.
 */
public class NullValidator {

    private static final List<TypeValidator> CUSTOM_VALIDATORS;

    static {
        // Load custom validators via ServiceLoader
        List<TypeValidator> validators = new ArrayList<>();
        ServiceLoader<TypeValidator> loader = ServiceLoader.load(TypeValidator.class);
        loader.forEach(validators::add);
        CUSTOM_VALIDATORS = Collections.unmodifiableList(validators);
    }

    private NullValidator() {
        // Utility class
    }

    /**
     * Validates that the given object and its entire object graph contains no null values.
     *
     * @param obj the object to validate
     * @throws NullsFoundException if any null value is found in the object graph
     */
    public static <T> T assertNoNulls(@Nullable T obj) throws NullsFoundException {
        List<String> nullPaths = findNullPaths(obj);
        if (!nullPaths.isEmpty()) {
            throw new NullsFoundException(nullPaths);
        }
        assert obj != null;
        return obj;
    }

    /**
     * Validates that the given object and its entire object graph contains no null values.
     *
     * @param obj the object to validate
     * @return a list of paths where nulls were found, empty if no nulls were found
     */
    public static List<String> findNullPaths(@Nullable Object obj) {
        List<String> nullPaths = new ArrayList<>();
        collectNullPaths(obj, "root", new HashSet<>(), nullPaths);

        return nullPaths;
    }

    /**
     * Checks if the object and its entire object graph contain no null values.
     *
     * @param obj the object to check
     * @return true if no nulls are found, false otherwise
     */
    public static boolean hasNoNulls(@Nullable Object obj) {
       return findNullPaths(obj).isEmpty();
    }

    /**
     * Internal helper to collect all null paths without throwing immediately.
     */
    private static void collectNullPaths(@Nullable Object obj, String path, Set<Integer> visited, List<String> nullPaths) {
        if (obj == null) {
            nullPaths.add(path);
            return;
        }

        // Avoid infinite recursion for circular references
        int identityHash = System.identityHashCode(obj);
        if (visited.contains(identityHash)) {
            return;
        }
        visited.add(identityHash);

        Class<?> clazz = obj.getClass();

        // Handle primitives and their wrappers - no further inspection needed
        if (isPrimitiveOrWrapper(clazz) || clazz == String.class) {
            return;
        }

        // Check custom validators first
        for (TypeValidator validator : CUSTOM_VALIDATORS) {
            if (validator.canHandle(clazz)) {
                RecursiveValidator recursiveValidator = NullValidator::collectNullPaths;
                validator.validate(obj, path, visited, nullPaths, recursiveValidator);
                return;
            }
        }


        switch (obj) {
            case Optional<?> optional -> {
                optional.ifPresent(value -> collectNullPaths(value, path, visited, nullPaths));
                return;
            }

            case Collection<?> collection -> {
                int index = 0;
                for (Object item : collection) {
                    collectNullPaths(item, path + "[" + index + "]", visited, nullPaths);
                    index++;
                }
                return;
            }

            case Map<?, ?> map -> {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    collectNullPaths(entry.getKey(), path + ".key[" + entry.getKey() + "]", visited, nullPaths);
                    collectNullPaths(entry.getValue(), path + "[" + entry.getKey() + "]", visited, nullPaths);
                }
                return;
            }

            case Record record -> {
                RecordComponent[] components = clazz.getRecordComponents();
                for (RecordComponent component : components) {
                    try {
                        var accessor = component.getAccessor();
                        boolean wasAccessible = accessor.canAccess(obj);
                        if (!wasAccessible) {
                            accessor.setAccessible(true);
                        }
                        try {
                            Object value = accessor.invoke(obj);
                            collectNullPaths(value, path + "." + component.getName(), visited, nullPaths);
                        } finally {
                            if (!wasAccessible) {
                                try {
                                    accessor.setAccessible(false);
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to access record component: " + component.getName() + " at " + path + ". Is the component accessible?", e);
                    }
                }
                return;
            }

            // no need to recurse into these, jdk classes are fine if they arte not null themselves, no need to look in their innards
            case Object object when object.getClass().getName().startsWith("java") -> {
                return;
            }

            // no need to recurse into these
            case Enum<?> enumeration -> {
                return;
            }

            default -> {
                // will get covered later
            }
        }

        // Handle Arrays
        if (clazz.isArray()) {
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                Object item = Array.get(obj, i);
                collectNullPaths(item, path + "[" + i + "]", visited, nullPaths);
            }
            return;
        }

        // Handle regular (non-record) classes by reflecting over fields
        // Iterate through class hierarchy to include inherited fields
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            Field[] fields = current.getDeclaredFields();
            for (Field field : fields) {
                // Skip static fields and synthetic fields (like this$0)
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || field.isSynthetic()) {
                    continue;
                }
                boolean wasAccessible = field.canAccess(obj);
                try {
                    if (!wasAccessible) {
                        field.setAccessible(true);
                    }
                    Object value = field.get(obj);
                    collectNullPaths(value, path + "." + field.getName(), visited, nullPaths);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to access field: " + field.getName() + "of type " + field.getClass() + " at " + path + ". Is the field accessible?", e);
                } finally {
                    // restore the original accessibility state
                    if (!wasAccessible) {
                        try {
                            field.setAccessible(false);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            current = current.getSuperclass();
        }
    }



    private static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == Boolean.class
                || clazz == Byte.class
                || clazz == Character.class
                || clazz == Short.class
                || clazz == Integer.class
                || clazz == Long.class
                || clazz == Float.class
                || clazz == Double.class
                || Number.class.isAssignableFrom(clazz)
                || clazz == Void.class;
    }
}
