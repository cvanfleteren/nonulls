package net.vanfleteren.nonulls.validator.vavr.spi;

import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import net.vanfleteren.nonulls.validator.vavr.NullValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NullValidatorVavrTest {

    @AllArgsConstructor
    static class Holder {
        String held;
    }

    @AllArgsConstructor
    public static class Rec {
        String a;
        String b;
    }

    @Test
    void vavrList_ofSimpleValues_hasNoNulls() {
        List<String> v = List.of("a", "b", "c");
        
        assertDoesNotThrow(() -> NullValidator.assertNoNulls(v));
        assertTrue(NullValidator.assertNoNulls(v).isSuccess());
    }

    @Test
    void vavrOption_none_hasNoNulls() {
        Option<String> none = Option.none();
        
        assertDoesNotThrow(() -> NullValidator.assertNoNulls(none));
        assertTrue(NullValidator.assertNoNulls(none).isSuccess());
    }

    @Test
    void vavrOption_some_withInnerNull_isDetected() {
        Option<Holder> some = Option.of(new Holder(null));
        
        Either<List<String>, Option<Holder>> e = NullValidator.assertNoNullsEither(some);
        // Path should be relative to root since Option passes through contained value without index
        assertEquals(List.of("root.held"), e.getLeft());
    }

    @Test
    void vavrList_withPojoContainingNullField_isDetected() {
        List<Holder> list = List.of(new Holder(null));

        Either<List<String>, List<Holder>> e = NullValidator.assertNoNullsEither(list);
        assertEquals(List.of("root[0].held"), e.getLeft()   );
    }

    @Test
    void vavrMap_withNullInValue_isDetected_withKeyPaths() {
        Map<String, Rec> map = LinkedHashMap.of(
                "a", new Rec("ok", null)
        );

        Either<List<String>, Map<String,Rec>> e = NullValidator.assertNoNullsEither(map);
        // For Vavr Map, keys are iterated with index for key path and value path uses key in brackets
        assertEquals(List.of("root[a].b"), e.getLeft());
    }

    // Records for Vavr Option tests
    public record InnerRec(String s) {}
    public record OptionHolder(Option<InnerRec> inner) {}

    @Test
    void recordWithVavrOption_fieldNull_reportsFieldPath() {
        OptionHolder holder = new OptionHolder(null);

        Either<List<String>, OptionHolder> e = NullValidator.assertNoNullsEither(holder);
        assertEquals(List.of("root.inner"), e.getLeft());
    }

    @Test
    void recordWithVavrOption_emptyOption_isAllowed() {
        OptionHolder holder = new OptionHolder(Option.none());

        assertTrue(NullValidator.assertNoNulls(holder).isSuccess());
    }

    @Test
    void recordWithVavrOption_someWithInnerNull_reportsFullPath() {
        OptionHolder holder = new OptionHolder(Option.of(new InnerRec(null)));

        Either<List<String>, OptionHolder> e = NullValidator.assertNoNullsEither(holder);
        assertEquals(List.of("root.inner.s"), e.getLeft());
    }

    @Test
    void recordWithVavrOption_someWithoutNulls_passes() {
        OptionHolder holder = new OptionHolder(Option.of(new InnerRec("ok")));

        assertTrue(NullValidator.assertNoNulls(holder).isSuccess());
    }
}
