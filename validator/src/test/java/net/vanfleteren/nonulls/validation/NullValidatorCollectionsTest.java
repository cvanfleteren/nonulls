package net.vanfleteren.nonulls.validation;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NullValidatorCollectionsTest {

    // Helper records for complex mixed collection tests

    record Outer(Inner inner) {
        record Inner(String s) {}
    }

    @Test
    void listContainingNulls_isDetectedWithIndexes() {
        List<String> data = Arrays.asList("a", null, "c");
        
        assertEquals(List.of("root[1]"), NullValidator.findNullPaths(data));
    }

    @Test
    void arrayContainingNulls_isDetectedWithIndexes() {
        String[] arr = new String[]{"x", null, "y"};
        
        assertEquals(List.of("root[1]"), NullValidator.findNullPaths(arr));
    }

    @Test
    void mapWithNullKeyAndValue_isDetectedWithKeyAndValuePaths() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(null, "x");
        map.put("k", null);
        // Maintain insertion order for deterministic assertion (LinkedHashMap)
        
        assertEquals(List.of("root.key[null]", "root[k]"), NullValidator.findNullPaths(map));
    }

    @Test
    void complexObject_multipleNulls_collectsAllNullPaths() {
        List<Object> list = new ArrayList<>();
        list.add(null);               // root[0]
        list.add(List.of("ok"));     // ok
        list.add(new Outer(new Outer.Inner(null))); // root[2].inner.s

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("a", list);          // contains two null paths inside
        map.put("b", null);          // root[b]

        // Expected order: map iteration order (a then b), and within list: index order
        
        assertEquals(List.of(
                "root[a][0]",
                "root[a][2].inner.s",
                "root[b]"
        ), NullValidator.findNullPaths(map));
    }
}
