package net.vanfleteren.nonulls.validation;

import java.lang.reflect.Array;
import java.lang.reflect.RecordComponent;
import java.util.*;

/**
 * Utility class for validating that an object graph contains no null values.
 * This includes recursively checking inside collections, arrays, optionals, pojos and records.
 */
public class NullValidator {

    private NullValidator() {
        // Utility class
    }

    /**
     * Validates that the given object and its entire object graph contains no null values.
     *
     * @param obj the object to validate
     * @throws NullsFoundException if any null value is found in the object graph
     */
    public static void assertNoNulls(Object obj) {
        List<String> nullPaths = new ArrayList<>();
        collectNullPaths(obj, "root", new HashSet<>(), nullPaths);
        if (!nullPaths.isEmpty()) {
            throw new NullsFoundException(nullPaths);
        }
    }

    /**
     * Internal helper to collect all null paths without throwing immediately.
     */
    private static void collectNullPaths(Object obj, String path, Set<Integer> visited, List<String> nullPaths) {
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

        // Handle Optional
        switch (obj) {
            case Optional<?> optional -> {
                optional.ifPresent(value -> collectNullPaths(value, path, visited, nullPaths));
                return;
            }

            // Handle Collections
            case Collection<?> collection -> {
                int index = 0;
                for (Object item : collection) {
                    collectNullPaths(item, path + "[" + index + "]", visited, nullPaths);
                    index++;
                }
                return;
            }

            // Handle Maps
            case Map<?, ?> map -> {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    collectNullPaths(entry.getKey(), path + ".key[" + entry.getKey() + "]", visited, nullPaths);
                    collectNullPaths(entry.getValue(), path + "[" + entry.getKey() + "]", visited, nullPaths);
                }
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

        // Handle Records
        if (clazz.isRecord()) {
            RecordComponent[] components = clazz.getRecordComponents();
            for (RecordComponent component : components) {
                try {
                    Object value = component.getAccessor().invoke(obj);
                    collectNullPaths(value, path + "." + component.getName(), visited, nullPaths);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to access record component: " + component.getName() + " at " + path+". Is the component public?", e);
                }
            }
            return;
        }

        // Handle regular (non-record) classes by reflecting over fields
        // Iterate through class hierarchy to include inherited fields
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            java.lang.reflect.Field[] fields = current.getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                // Skip static fields and synthetic fields (like this$0)
                int modifiers = field.getModifiers();
                if (java.lang.reflect.Modifier.isStatic(modifiers) || field.isSynthetic()) {
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
                    throw new RuntimeException("Failed to access field: " + field.getName() + " at " + path + ". Is the field accessible?", e);
                } finally {
                    // restore original accessibility state
                    if (!wasAccessible) {
                        try { field.setAccessible(false); } catch (Exception ignored) {}
                    }
                }
            }
            current = current.getSuperclass();
        }
    }

    /**
     * Checks if the object and its entire object graph contain no null values.
     *
     * @param obj the object to check
     * @return true if no nulls are found, false otherwise
     */
    public static boolean hasNoNulls(Object obj) {
        try {
            assertNoNulls(obj);
            return true;
        } catch (NullsFoundException e) {
            return false;
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
