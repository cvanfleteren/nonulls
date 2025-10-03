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
        
        assertEquals(List.of("root.inner.s"), NullValidator.findNullPaths(obj));
    }

    @Test
    void optionalEmpty_isAllowed() {
        Optional<String> empty = Optional.empty();
        
        assertEquals(java.util.List.of(), NullValidator.findNullPaths(empty));
    }

    @Test
    void optionalWithNestedRecordContainingNull_reportsWithGetPath() {
        Optional<Inner> opt = Optional.of(new Inner(null));
        
        assertEquals(List.of("root.s"), NullValidator.findNullPaths(opt));
    }

    @Test
    void recordOptional_innerIsNull_reportsFieldPath() {
        OptionalHolder holder = new OptionalHolder(null);
        
        assertEquals(List.of("root.inner"), NullValidator.findNullPaths(holder));
    }

    @Test
    void recordOptional_emptyIsAllowed() {
        OptionalHolder holder = new OptionalHolder(Optional.empty());
        
        assertEquals(java.util.List.of(), NullValidator.findNullPaths(holder));
    }

    @Test
    void recordOptional_presentInnerWithNull_reportsWithGetPath() {
        OptionalHolder holder = new OptionalHolder(Optional.of(new Inner(null)));
        
        assertEquals(List.of("root.inner.s"), NullValidator.findNullPaths(holder));
    }

    @Test
    void recordOptional_presentInnerWithoutNulls_passes() {
        OptionalHolder holder = new OptionalHolder(Optional.of(new Inner("inner")));
        
        assertEquals(java.util.List.of(), NullValidator.findNullPaths(holder));
    }

    @Test
    void recordWithCollections_nullElements_reportIndexesAndKeys() {
        List<String> list = Arrays.asList("a", null, "c");
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("x", 1);
        map.put("y", null);   // null value -> root.map[y]
        map.put(null, 2);      // null key -> root.map.key[null]
        WithCollections rec = new WithCollections(list, map);

        assertEquals(List.of("root.list[1]", "root.map[y]", "root.map.key[null]"), NullValidator.findNullPaths(rec));
    }

    @Test
    void recordWithCollections_nullFields_reportFieldPaths() {
        WithCollections rec = new WithCollections(null, null);
        
        assertEquals(List.of("root.list", "root.map"), NullValidator.findNullPaths(rec));
    }

    @Test
    void recordWithCollections_noNulls_passes() {
        WithCollections rec = new WithCollections(List.of("a", "b"), Map.of("k", 1));
        
        assertEquals(java.util.List.of(), NullValidator.findNullPaths(rec));
    }
}
