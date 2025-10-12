package net.vanfleteren.nonulls.validator.spi;

import java.util.List;
import java.util.Set;

/**
 * SPI for registering custom type validators.
 * Implementations can provide special handling for specific types
 * without requiring the core validator to have dependencies on those types.
 */
public interface TypeValidator {
    /**
     * Determines if this validator can handle the given class.
     *
     * @param clazz the class to check
     * @return true if this validator should handle instances of this class
     */
    boolean canHandle(Class<?> clazz);

    /**
     * Validates an object of a supported type, collecting any null paths found.
     *
     * @param obj the object to validate (never null - already checked by caller)
     * @param path the current path in the object graph
     * @param visited set of already-visited object identity hashes (for cycle detection)
     * @param nullPaths list to collect paths where nulls are found
     * @param recursiveValidator callback to recursively validate nested objects
     */
    void validate(Object obj, String path, Set<Integer> visited,
                  List<String> nullPaths, RecursiveValidator recursiveValidator);
}