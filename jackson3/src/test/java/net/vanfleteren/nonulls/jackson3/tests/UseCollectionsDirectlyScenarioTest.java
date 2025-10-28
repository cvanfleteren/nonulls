package net.vanfleteren.nonulls.jackson3.tests;

import net.vanfleteren.nonulls.jackson3.tests.support.JacksonTest;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;

import java.util.*;

import static net.vanfleteren.nonulls.jackson3.tests.support.JsonAssert.assertThatJson;

public class UseCollectionsDirectlyScenarioTest extends JacksonTest {

    @Test
    void map_no_nulls() {
        assertThatJson("""
                {
                    "key1":"1"
                }
                """
        )
        .whenReadAs(new TypeReference<Map<String,String>>() {})
        .deserializesInto(
            Map.of("key1", "1")
        );
    }

    @Test
    void map_nulls_and_empty_strings_are_ignored() {
        assertThatJson("""
                {
                    "key1": 1,
                    "key2": "",
                    "key3": null
                }
                """
        )
        .whenReadAs(new TypeReference<Map<String,String>>() {})
        .deserializesInto(
                Map.of("key1", "1")
        );
    }

    @Test
    void map_is_null_returns_empty_map() {
        assertThatJson("""
                null
                """
        )
        .whenReadAs(new TypeReference<Map<String,String>>() {})
        .deserializesInto(
                Map.of()
        );
    }

    @Test
    void list_nulls_are_ignored() {
       assertThatJson("""
                [null, "1"]
                """
        )
       .whenReadAs(new TypeReference<List<String>>() {})
       .deserializesInto(
                List.of("1")
        );
    }

    @Test
    void list_is_null_returns_empty_list() {
           assertThatJson("""
                null
                """
        )
       .whenReadAs(new TypeReference<List<String>>() {})
       .deserializesInto(
                List.of()
        );
    }

    @Test
    void array_has_null_returns_filtered_array () {
        assertThatJson("""
                 [null, "1"]
                """
        )
                .whenReadAs(new TypeReference<String[]>() {})
                .deserializesInto(
                        new String[]{"1"}
                );
    }

    @Test
    void array_is_null_returns_empty_array () {
        assertThatJson("""
                 null
                """
        )
                .whenReadAs(new TypeReference<String[]>() {})
                .deserializesInto(new String[]{});
        ;

    }


    @Test
    void set_nulls_are_ignored() {
        assertThatJson("""
                 [null, "1"]
                """
        )
        .whenReadAs(new TypeReference<Set<String>>() {})
        .deserializesInto(
            Set.of("1")
        );
    }

    @Test
    void set_is_null_returns_empty_set() {
        assertThatJson("""
                 null
                """
        )
        .whenReadAs(new TypeReference<Set<String>>() {})
        .deserializesInto(
                Set.of()
        );
    }

    @Test
    void optional_has_value() {
        assertThatJson("""
                 "foo"
                """
        )
        .whenReadAs(new TypeReference<Optional<String>>() {})
        .deserializesInto(
                Optional.of("foo")
        );
    }

    @Test
    void optional_empty_string() {
        assertThatJson("""
                 ""
                """
        )
        .whenReadAs(new TypeReference<Optional<String>>() {})
        .deserializesInto(
                Optional.empty()
        );
    }

    @Test
    void optional_ofListOfString_empty_string() {
        assertThatJson("""
                 [null]
                """
        )
        .whenReadAs(new TypeReference<Optional<List<String>>>() {})
        .deserializesInto(
                Optional.of(List.of()) // TODO what do we want to do here?
        );
    }

}
