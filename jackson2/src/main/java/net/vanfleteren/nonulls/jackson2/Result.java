package net.vanfleteren.nonulls.jackson2;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.vanfleteren.nonulls.validation.NullsFoundException;

import java.util.Optional;

/**
 * A Result object that can either be a Success or a Failure.
 * This is a convenient type to represent the result of a deserialization operation.
 * If you wrap the actual type that you except to read from the ObjectMapper in a Result, you'll always get a Success or a Failure, indicating what went wrong in the deserialization.
 * There are two main reasons for failures: invalid JSON or nulls found in the resulting graph.
 * <p>
 * So instead of <code>objectMapper.readValue(json, MyPojo.class)</code>, you can use something like this:
 * {@snippet :
  Result<MyPojo> result = objectMapper.readValue(json, new TypeReference(){})
  switch(result) {
     case Success<MyPojo> s -> s.value() // do something with the result
     case Failure<MyPojo> f -> {} // handle failure
  }
 * }
 *
 *
 * @param <T>
 */
public sealed interface Result<T> {

    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    static <T> Result<T> invalidJson(JsonProcessingException exception) {
        return new InvalidJson<>(exception);
    }

    static <T> Result<T> nullsFound(NullsFoundException exception) {
        return new NullsFound<>(exception);
    }

    default boolean isSuccess() {
        return this instanceof Success;
    }

    default boolean isFailure() {
        return this instanceof Failure;
    }

    default Optional<T> get() {
        return switch (this) {
            case Success<T> s -> Optional.of(s.value());
            case Failure<T> f -> Optional.empty();
        };
    }

    record Success<T>(T value) implements Result<T> {}

    sealed interface Failure<T> extends Result<T> {
        Exception exception();
    }

    record InvalidJson<T>(JsonProcessingException exception) implements Failure<T> {}

    record NullsFound<T>(NullsFoundException exception) implements Failure<T> {}

}
