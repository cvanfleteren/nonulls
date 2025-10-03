package net.vanfleteren.nonulls.validation.vavr.spi;

import io.vavr.collection.List;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import net.vanfleteren.nonulls.validation.NullValidator;
import net.vanfleteren.nonulls.validation.NullsFoundException;
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
        assertTrue(NullValidator.hasNoNulls(v));
    }

    @Test
    void vavrOption_none_hasNoNulls() {
        Option<String> none = Option.none();
        
        assertDoesNotThrow(() -> NullValidator.assertNoNulls(none));
        assertTrue(NullValidator.hasNoNulls(none));
    }

    @Test
    void vavrOption_some_withInnerNull_isDetected() {
        Option<Holder> some = Option.of(new Holder(null));
        
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(some));
        // Path should be relative to root since Option passes through contained value without index
        assertEquals(java.util.List.of("root.held"), ex.getNullPaths());
    }

    @Test
    void vavrList_withPojoContainingNullField_isDetected() {
        List<Holder> list = List.of(new Holder(null));
        
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(list));
        assertEquals(java.util.List.of("root[0].held"), ex.getNullPaths());
    }

    @Test
    void vavrMap_withNullInValue_isDetected_withKeyPaths() {
        Map<String, Rec> map = LinkedHashMap.of(
                "a", new Rec("ok", null)
        );
        
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(map));
        // For Vavr Map, keys are iterated with index for key path and value path uses key in brackets
        assertEquals(java.util.List.of("root[a].b"), ex.getNullPaths());
    }

    // Records for Vavr Option tests
    public record InnerRec(String s) {}
    public record OptionHolder(Option<InnerRec> inner) {}

    @Test
    void recordWithVavrOption_fieldNull_reportsFieldPath() {
        OptionHolder holder = new OptionHolder(null);
        
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(holder));
        assertEquals(java.util.List.of("root.inner"), ex.getNullPaths());
    }

    @Test
    void recordWithVavrOption_emptyOption_isAllowed() {
        OptionHolder holder = new OptionHolder(Option.none());
        
        assertDoesNotThrow(() -> NullValidator.assertNoNulls(holder));
        assertTrue(NullValidator.hasNoNulls(holder));
    }

    @Test
    void recordWithVavrOption_someWithInnerNull_reportsFullPath() {
        OptionHolder holder = new OptionHolder(Option.of(new InnerRec(null)));
        
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(holder));
        assertEquals(java.util.List.of("root.inner.s"), ex.getNullPaths());
    }

    @Test
    void recordWithVavrOption_someWithoutNulls_passes() {
        OptionHolder holder = new OptionHolder(Option.of(new InnerRec("ok")));
        
        assertDoesNotThrow(() -> NullValidator.assertNoNulls(holder));
        assertTrue(NullValidator.hasNoNulls(holder));
    }
}
