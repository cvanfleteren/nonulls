package net.vanfleteren.nonulls.validator;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NullValidatorPojosTest {

    @AllArgsConstructor
    @NoArgsConstructor
    protected static class PlainPojo {
        private String s;
        FooEnum fooEnum;
        LocalDate localDate;
        UUID uuid;
        Locale locale;
        URL url;
        URI uri;
    }

    @AllArgsConstructor
    public static class InnerPojo {
        protected String s;
    }

    @AllArgsConstructor
    private static class OuterPojo {
        InnerPojo inner;
    }

    @AllArgsConstructor
    static class CollectionsPojo {
        public List<String> list;
        private Map<String, Integer> map;
        String[] array;
    }

    @AllArgsConstructor
    static class OptionalPojo {
        Optional<InnerPojo> inner;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    static class ParentPojo {
        String p;
    }

    @AllArgsConstructor
    static class ChildPojo extends ParentPojo {
        String c;
    }

    enum FooEnum {A, B}


    @Test
    void pojo_withNullField_reportsFieldPath() {
        PlainPojo p = new PlainPojo();

        assertEquals(List.of("root.s", "root.fooEnum", "root.localDate","root.uuid", "root.locale", "root.url", "root.uri"), NullValidator.findNullPaths(p));
    }

    @Test
    void pojo_noNullsPresent() throws MalformedURLException {
        PlainPojo p = new PlainPojo("s", FooEnum.A, LocalDate.now(), UUID.randomUUID(), Locale.ENGLISH, URI.create("http://localhost").toURL(), URI.create("http://localhost"));

        assertEquals(List.of(), NullValidator.findNullPaths(p));
    }

    @Test
    void nestedPojo_withNullInInner_reportsNestedPath() {
        OuterPojo o = new OuterPojo(new InnerPojo(null));

        assertEquals(List.of("root.inner.s"), NullValidator.findNullPaths(o));
    }

    @Test
    void pojo_withCollectionsAndArray_reportsCorrectIndexesAndKeys() {
        List<String> list = Arrays.asList("a", null);
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("x", 1);
        map.put("y", null);
        String[] arr = new String[]{"ok", null};
        CollectionsPojo pojo = new CollectionsPojo(list, map, arr);

        assertEquals(List.of("root.list[1]", "root.map[y]", "root.array[1]"), NullValidator.findNullPaths(pojo));
    }

    @Test
    void inheritedFields_areAlsoChecked() {
        ChildPojo child = new ChildPojo("c");

        assertEquals(List.of("root.p"), NullValidator.findNullPaths(child));
    }

    @Test
    void pojo_optionalField_nullOptional_reportsFieldPath() {
        OptionalPojo holder = new OptionalPojo(null);

        assertEquals(List.of("root.inner"), NullValidator.findNullPaths(holder));
    }

    @Test
    void pojo_optionalField_emptyIsAllowed() {
        OptionalPojo holder = new OptionalPojo(Optional.empty());

        assertEquals(List.of(), NullValidator.findNullPaths(holder));
    }

    @Test
    void pojo_optionalField_presentInnerWithNull_reportsWithGetPath() {
        OptionalPojo holder = new OptionalPojo(Optional.of(new InnerPojo(null)));

        assertEquals(List.of("root.inner.s"), NullValidator.findNullPaths(holder));
    }

    @Test
    void pojo_optionalField_presentInnerWithoutNulls_passes() {
        OptionalPojo holder = new OptionalPojo(Optional.of(new InnerPojo("ok")));

        assertEquals(List.of(), NullValidator.findNullPaths(holder));
    }

    @Test
    void pojo_withoutNulls_passes() {
        CollectionsPojo ok = new CollectionsPojo(List.of("a"), Map.of("k", 1), new String[]{"v"});

        assertEquals(List.of(), NullValidator.findNullPaths(ok));
    }
}
