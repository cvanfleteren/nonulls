package net.vanfleteren.nonulls.validation;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

class NullValidatorPojosTest {

    // Regular classes (POJOs) to validate reflection-based traversal

    
    @AllArgsConstructor
    static class PlainPojo {
        String s;
    }
    @AllArgsConstructor
    static class InnerPojo {
        String s;
    }
    @AllArgsConstructor
    static class OuterPojo {
        InnerPojo inner;
    }
    @AllArgsConstructor
    static class CollectionsPojo {
        List<String> list;
        Map<String, Integer> map;
        String[] array;
    }
    @AllArgsConstructor
    static class OptionalPojo {
        Optional<InnerPojo> inner;
    }
    @AllArgsConstructor
    @NoArgsConstructor
    static class ParentPojo { String p; }
    @AllArgsConstructor
    static class ChildPojo extends ParentPojo { String c; }

    @Test
    void pojo_withNullField_reportsFieldPath() {
        PlainPojo p = new PlainPojo(null);
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(p));
        assertEquals(List.of("root.s"), ex.getNullPaths());
    }

    @Test
    void nestedPojo_withNullInInner_reportsNestedPath() {
        OuterPojo o = new OuterPojo(new InnerPojo(null));
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(o));
        assertEquals(List.of("root.inner.s"), ex.getNullPaths());
    }

    @Test
    void pojo_withCollectionsAndArray_reportsCorrectIndexesAndKeys() {
        List<String> list = Arrays.asList("a", null);
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("x", 1);
        map.put("y", null);
        String[] arr = new String[]{"ok", null};
        CollectionsPojo pojo = new CollectionsPojo(list, map, arr);
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(pojo));
        assertEquals(List.of("root.list[1]", "root.map[y]", "root.array[1]"), ex.getNullPaths());
    }

    @Test
    void inheritedFields_areAlsoChecked() {
        ChildPojo child = new ChildPojo("c");
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(child));
        assertEquals(List.of("root.p"), ex.getNullPaths());
    }

    @Test
    void pojo_optionalField_nullOptional_reportsFieldPath() {
        OptionalPojo holder = new OptionalPojo(null);
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(holder));
        assertEquals(List.of("root.inner"), ex.getNullPaths());
    }

    @Test
    void pojo_optionalField_emptyIsAllowed() {
        OptionalPojo holder = new OptionalPojo(Optional.empty());
        assertDoesNotThrow(() -> NullValidator.assertNoNulls(holder));
        assertTrue(NullValidator.hasNoNulls(holder));
    }

    @Test
    void pojo_optionalField_presentInnerWithNull_reportsWithGetPath() {
        OptionalPojo holder = new OptionalPojo(Optional.of(new InnerPojo(null)));
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(holder));
        assertEquals(List.of("root.inner.s"), ex.getNullPaths());
    }

    @Test
    void pojo_optionalField_presentInnerWithoutNulls_passes() {
        OptionalPojo holder = new OptionalPojo(Optional.of(new InnerPojo("ok")));
        assertDoesNotThrow(() -> NullValidator.assertNoNulls(holder));
        assertTrue(NullValidator.hasNoNulls(holder));
    }

    @Test
    void pojo_withoutNulls_passes() {
        CollectionsPojo ok = new CollectionsPojo(List.of("a"), Map.of("k", 1), new String[]{"v"});
        assertDoesNotThrow(() -> NullValidator.assertNoNulls(ok));
        assertTrue(NullValidator.hasNoNulls(ok));
    }
}
