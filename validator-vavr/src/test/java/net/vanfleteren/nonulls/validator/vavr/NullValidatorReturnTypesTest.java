package net.vanfleteren.nonulls.validator.vavr;

import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NullValidatorReturnTypesTest {

    @Test
    void assertNoNulls_returnsTry_onSuccess() {
        // success
        String value = "ok";
        Try<String> resOk = NullValidator.assertNoNulls(value);
        
        assertTrue(resOk.isSuccess());
        assertEquals("ok", resOk.get());

        // failure
        List<String> vavrListWithNull = List.of("x", null);
        Try<List<String>> failed = NullValidator.assertNoNulls(vavrListWithNull);
        assertTrue(failed.isFailure());
        assertInstanceOf(NullsFoundException.class, failed.getCause());
        assertEquals(((NullsFoundException) failed.getCause()).getNullPaths(), List.of("root[1]"));
    }

    @Test
    void whenNoNulls_returnsOption_onSuccessAndNoneOnFailure() {
        // success -> Some
        Integer value = 42;
        Option<Integer> some = NullValidator.whenNoNulls(value);
        
        assertTrue(some.isDefined());
        assertEquals(42, some.get());

        // failure -> None
        List<String> vavrListWithNull = List.of("x", null);
        Option<List<String>> none = NullValidator.whenNoNulls(vavrListWithNull);
        assertTrue(none.isEmpty());
    }

    @Test
    void validate_returnsValidation_valid() {
        // success -> Valid
        var ok = java.util.Map.of("k", 1);
        Validation<List<String>, java.util.Map<String, Integer>> valid = NullValidator.validate(ok);
        
        assertNotNull(valid);
        assertTrue(valid.isValid());
        assertEquals(ok, valid.get());

        // failure -> Invalid
        List<Integer> vavrListWithNull = List.of(5, null);
        Validation<List<String>, List<Integer>> invalid = NullValidator.validate(vavrListWithNull);
        assertTrue(invalid.isInvalid());
        assertEquals(invalid.getError(), List.of("root[1]"));
    }
}
