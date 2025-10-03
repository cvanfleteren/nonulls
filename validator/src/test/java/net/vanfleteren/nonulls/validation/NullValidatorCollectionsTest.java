package net.vanfleteren.nonulls.validation;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class NullValidatorCollectionsTest {

    // Helper records for complex mixed collection tests
    record Inner(String s) {}
    record Outer(Inner inner) {}

    @Test
    void listContainingNulls_isDetectedWithIndexes() {
        List<String> data = Arrays.asList("a", null, "c");
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(data));
        assertEquals(List.of("root[1]"), ex.getNullPaths());
        assertTrue(ex.getMessage().contains("root[1]"));
    }

    @Test
    void arrayContainingNulls_isDetectedWithIndexes() {
        String[] arr = new String[]{"x", null, "y"};
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(arr));
        assertEquals(List.of("root[1]"), ex.getNullPaths());
    }

    @Test
    void mapWithNullKeyAndValue_isDetectedWithKeyAndValuePaths() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(null, "x");
        map.put("k", null);
        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(map));
        // Maintain insertion order for deterministic assertion (LinkedHashMap)
        assertEquals(List.of("root.key[null]", "root[k]"), ex.getNullPaths());
    }

    @Test
    void complexObject_multipleNulls_collectsAllNullPaths() {
        List<Object> list = new ArrayList<>();
        list.add(null);               // root[0]
        list.add(List.of("ok"));     // ok
        list.add(new Outer(new Inner(null))); // root[2].inner.s

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("a", list);          // contains two null paths inside
        map.put("b", null);          // root[b]

        NullsFoundException ex = assertThrows(NullsFoundException.class, () -> NullValidator.assertNoNulls(map));
        // Expected order: map iteration order (a then b), and within list: index order
        assertEquals(List.of(
                "root[a][0]",
                "root[a][2].inner.s",
                "root[b]"
        ), ex.getNullPaths());
    }
}
