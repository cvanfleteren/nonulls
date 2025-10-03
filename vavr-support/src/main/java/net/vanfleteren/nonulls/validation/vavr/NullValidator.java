package net.vanfleteren.nonulls.validation.vavr;

import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.control.Validation;

/**
 * A Vavr friendly wrapper around the non-nulls validation library that returns Vavr types instead of throwing an exception.
 */
public class NullValidator {

    /**
     * Returns a Success with the value if there is no null in the graph, otherwise returns Failure.
     */
    public static <T> Try<T> assertNoNulls(T value) {
        try {
            net.vanfleteren.nonulls.validation.NullValidator.assertNoNulls(value);
            return Try.success(value);
        }catch (net.vanfleteren.nonulls.validation.NullsFoundException e) {
            return Try.failure(new NullsFoundException(List.ofAll(e.getNullPaths())));
        }
    }

    /**
     * Returns an Option with the value if there is no null in the graph, otherwise returns None.
     */
    public static <T> Option<T> whenNoNulls(T value) {
        if(net.vanfleteren.nonulls.validation.NullValidator.hasNoNulls(value)) {
            return Option.some(value);
        } else {
            return Option.none();
        }
    }

    /**
     * Returns a Validation with the value if there is no null in the graph, otherwise returns Invalid with the list of paths where nulls were found.
     */
    public static <T> Validation<List<String>, T> validate(T value) {
        Try<T> result = assertNoNulls(value);
        return result.fold(
                ex -> Validation.invalid(List.ofAll(((net.vanfleteren.nonulls.validation.vavr.NullsFoundException) ex).getNullPaths())),
                Validation::valid
        );
    }
}