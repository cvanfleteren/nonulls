package net.vanfleteren.nonulls.validation.vavr;

import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import org.jspecify.annotations.Nullable;

/**
 * A Vavr friendly wrapper around the non-nulls validation library that returns Vavr types instead of throwing an exception.
 */
public class NullValidator {

    /**
     * Returns a Success with the value if there is no null in the graph, otherwise returns Failure.
     */
    public static <T> Try<T> assertNoNulls(@Nullable T value) {
        java.util.List<String> nullPaths = net.vanfleteren.nonulls.validation.NullValidator.findNullPaths(value);

        if(nullPaths.isEmpty()) {
            assert value != null;
           return Try.success(value);
        } else {
            return Try.failure(new NullsFoundException(List.ofAll(nullPaths)));
        }
    }

    /**
     * Returns an Option with the value if there is no null in the graph, otherwise returns None.
     */
    public static <T> Option<T> whenNoNulls(@Nullable T value) {
        if(net.vanfleteren.nonulls.validation.NullValidator.hasNoNulls(value)) {
            return Option.of(value);
        } else {
            return Option.none();
        }
    }

    /**
     * Returns a Validation with the value if there is no null in the graph, otherwise returns Invalid with the list of paths where nulls were found.
     */
    public static <T> Validation<List<String>, T> validate(@Nullable T value) {
        java.util.List<String> nullPaths = net.vanfleteren.nonulls.validation.NullValidator.findNullPaths(value);

        if(nullPaths.isEmpty()) {
            assert value != null;
            return Validation.valid(value);
        } else {
            return Validation.invalid(List.ofAll(nullPaths));
        }
    }
}