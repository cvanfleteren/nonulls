package net.vanfleteren.nonulls.jackson2.tests;

import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.vanfleteren.nonulls.jackson2.tests.support.JacksonTest;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static net.vanfleteren.nonulls.jackson2.tests.support.JsonAssert.assertThatJson;

public class ScenarioTest extends JacksonTest {

    record Data(String s, Optional<String> optional, List<String> list, Set<String> set, Map<String, Integer> map,
                Map<String, Optional<String>> optMap) {
    }

    @Test
    void scenarioTest1() {
        assertThatJson("""
                {
                    "s":"s"
                }
                """
        ).deserializesInto(
                new Data("s", empty(), List.of(), Set.of(), Map.of(), Map.of())
        );
    }

    @Test
    void scenarioTest2() {
        assertThatJson("""
                {
                    "s":"s",
                    "optional": null,
                    "list": null,
                    "set": null,
                    "map": null
                }
                """
        ).deserializesInto(
                new Data("s", empty(), List.of(), Set.of(), Map.of(), Map.of())
        );
    }

    @Test
    void scenarioTest3() {
        record Data(Map<String, Optional<String>> optMap) {
        }

        assertThatJson("""
                {
                    "optMap": {
                        "key1": "",
                        "key2": null,
                        "key3": "3"
                    }
                }
                """
        ).deserializesInto(
                new Data(
                        Map.of(
                                "key1", empty(),
                                "key2", empty(),
                                "key3", of("3")
                        )
                )
        );
    }

    @Test
    void scenarioTest4() {
        record Data(Map<String, Integer> map) {
        }

        assertThatJson("""
                {
                    "map": {
                        "key1": 1,
                        "key2": null,
                        "key3": 3
                    }
                }
                """
        ).using(b -> b.filterNullValuesInMaps(false))
                .deserializesInto(
                        new Data(
                                mutableMapOf("key3", 3, "key2", null, "key1", 1)
                        )
                );
    }

    @Test
    void scenarioTest4_2() {
        record Data(Map<String, Integer> map) {
        }

        assertThatJson("""
                {
                    "map": {
                        "key1": 1,
                        "key2": null,
                        "key3": 3
                    }
                }
                """
        ).using(b -> b.filterNullValuesInMaps(true))
                .deserializesInto(
                        new Data(
                                mutableMapOf("key1", 1, "key3", 3)
                        )
                );
    }

    @Test
    void scenarioTest5() {

        record Data(Map<String, Optional<String>> optMap) {
        }

        assertThatJson("""
                {
                    "optMap": {
                        "opt1": null,
                        "opt2": "",
                        "opt3": "opt3"
                    }
                }
                """
        ).using(b -> b.disableAll().emptyAwareOptional(true).filterNullsInCollections(true))
                .deserializesInto(
                        new Data(
                                Map.of("opt1", empty(), "opt2", empty(), "opt3", of("opt3"))
                        )
                );
    }


    @lombok.Data
    @NoArgsConstructor
    static class Data2 {
        @JsonMerge
        @JsonProperty("map")
        final ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

        {
            map.put("key1", 1);
        }
    }

    @Test
    void scenarioTest6() {


        Data2 expected = new Data2();
        expected.map.put("key1", 1);
        expected.map.put("key3", 3);

        assertThatJson("""
                {
                    "map": {
                        "key3": 3
                    }
                }
                """
        )
        .deserializesInto(
                expected
        );
    }

    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> mutableMapOf(Object... entries) {
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            map.put((K) entries[i], (V) entries[i + 1]);
        }
        return map;
    }

}
