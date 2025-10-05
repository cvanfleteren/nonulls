package net.vanfleteren.nonulls.validation.vavr;

import io.vavr.collection.List;

import java.util.Objects;

/**
 * Exception thrown when one or more null values are found in an object graph.
 * Contains the list of paths where nulls were detected.
 */
public class NullsFoundException extends RuntimeException {
    private final List<String> nullPaths;

    NullsFoundException(List<String> nullPaths) {
        super(buildMessage(nullPaths));
        this.nullPaths = Objects.requireNonNull(nullPaths, "nullPaths");
    }

    private static String buildMessage(List<String> nullPaths) {
        return "Null value(s) found at: " + String.join(", ", nullPaths);
    }

    /**
     * Returns the paths in the object graph where nulls were found.
     */
    public List<String> getNullPaths() {
        return nullPaths;
    }
}
