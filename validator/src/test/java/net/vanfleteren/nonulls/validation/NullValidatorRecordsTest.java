package net.vanfleteren.nonulls.validation;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class NullValidatorRecordsTest {

    // Simple records for nested record tests
    record Inner(String s) {}
    record Outer(Inner inner) {}
    record WithCollections(List<String> list, Map<String, Integer> map) {}
    record OptionalHolder(Optional<Inner> inner) {}

    @Test
    void nestedRecords_nullInInnerField_isReportedWithFullPath() {
        Outer obj = new Outer(new Inner(null));
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(obj));
        assertEquals(List.of("root.inner.s"), ex.getNullPaths());
    }

    @Test
    void optionalEmpty_isAllowed() {
        Optional<String> empty = Optional.empty();
        assertDoesNotThrow(() -> NullValidator.assertNoNulls(empty));
        assertTrue(NullValidator.hasNoNulls(empty));
    }

    @Test
    void optionalWithNestedRecordContainingNull_reportsWithGetPath() {
        Optional<Inner> opt = Optional.of(new Inner(null));
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(opt));
        assertEquals(List.of("root.s"), ex.getNullPaths());
    }

    @Test
    void recordOptional_innerIsNull_reportsFieldPath() {
        OptionalHolder holder = new OptionalHolder(null);
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(holder));
        assertEquals(List.of("root.inner"), ex.getNullPaths());
    }

    @Test
    void recordOptional_emptyIsAllowed() {
        OptionalHolder holder = new OptionalHolder(Optional.empty());
        assertDoesNotThrow(() -> NullValidator.assertNoNulls(holder));
        assertTrue(NullValidator.hasNoNulls(holder));
    }

    @Test
    void recordOptional_presentInnerWithNull_reportsWithGetPath() {
        OptionalHolder holder = new OptionalHolder(Optional.of(new Inner(null)));
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(holder));
        assertEquals(List.of("root.inner.s"), ex.getNullPaths());
    }

    @Test
    void recordOptional_presentInnerWithoutNulls_passes() {
        OptionalHolder holder = new OptionalHolder(Optional.of(new Inner("inner")));
        assertDoesNotThrow(() -> NullValidator.assertNoNulls(holder));
        assertTrue(NullValidator.hasNoNulls(holder));
    }

    @Test
    void recordWithCollections_nullElements_reportIndexesAndKeys() {
        List<String> list = Arrays.asList("a", null, "c");
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("x", 1);
        map.put("y", null);   // null value -> root.map[y]
        map.put(null, 2);      // null key -> root.map.key[null]
        WithCollections rec = new WithCollections(list, map);

        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(rec));
        assertEquals(List.of("root.list[1]", "root.map[y]", "root.map.key[null]"), ex.getNullPaths());
    }

    @Test
    void recordWithCollections_nullFields_reportFieldPaths() {
        WithCollections rec = new WithCollections(null, null);
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(rec));
        assertEquals(List.of("root.list", "root.map"), ex.getNullPaths());
    }

    @Test
    void recordWithCollections_noNulls_passes() {
        WithCollections rec = new WithCollections(List.of("a", "b"), Map.of("k", 1));
        assertDoesNotThrow(() -> NullValidator.assertNoNulls(rec));
        assertTrue(NullValidator.hasNoNulls(rec));
    }
}
