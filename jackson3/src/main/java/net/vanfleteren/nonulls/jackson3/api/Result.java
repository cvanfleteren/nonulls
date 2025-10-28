package net.vanfleteren.nonulls.jackson3.api;

import net.vanfleteren.nonulls.validator.NullsFoundException;
import tools.jackson.core.JacksonException;

import java.util.Objects;
import java.util.Optional;

/**
 * A Result object that can either be a Success or a Failure.
 * This is a convenient type to represent the result of a deserialization operation.
 * If you wrap the actual type that you expect to read from the ObjectMapper in a Result, you'll always get a Success or a Failure, indicating what went wrong in the deserialization.
 * There are two main reasons for failures: invalid JSON or nulls found in the resulting graph.
 *
 * @param <T> contained value type
 */
public sealed interface Result<T> {

    /**
     * A Success Result with the given value.
     */
    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    /**
     * A Failure because of invalid JSON.
     */
    static <T> Result<T> invalidJson(JacksonException exception) {
        return new InvalidJson<>(exception);
    }

    /**
     * A Failure because of nulls found in the resulting graph.
     */
    static <T> Result<T> nullsFound(NullsFoundException exception) {
        return new NullsFound<>(exception);
    }

    /**
     * Returns true if this is a Success.
     */
    default boolean isSuccess() {
        return this instanceof Success;
    }

    /**
     * Returns true if this is a Failure.
     */
    default boolean isFailure() {
        return this instanceof Failure;
    }

    /**
     * Returns the contained value if this is a Success, or an empty Optional if this is a Failure.
     */
    default Optional<T> toOptional() {
        return switch (this) {
            case Success<T> s -> Optional.of(s.value());
            case Failure<T> f -> Optional.empty();
        };
    }

    record Success<T>(T value) implements Result<T> {
        public Success {
            Objects.requireNonNull(value);
        }
    }

    sealed interface Failure<T> extends Result<T> {
        Exception exception();
    }

    record InvalidJson<T>(JacksonException exception) implements Failure<T> {}

    record NullsFound<T>(NullsFoundException exception) implements Failure<T> {}

}
