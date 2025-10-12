package net.vanfleteren.nonulls.validator.spi;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Callback interface that allows TypeValidator implementations
 * to recursively validate nested objects.
 */
@FunctionalInterface
public interface RecursiveValidator {
    void validate(@Nullable Object obj, String path, Set<Integer> visited, List<String> nullPaths);
}