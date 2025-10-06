package net.vanfleteren.nonulls.jackson2;

import net.vanfleteren.nonulls.jackson2.support.JacksonTest;
import org.junit.jupiter.api.Test;

import java.util.*;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static net.vanfleteren.nonulls.jackson2.support.JsonAssert.assertThatJson;

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
        assertThatJson("""
                {
                    "s":"s",
                    "optional":null,
                    "list":null,
                    "set":null,
                    "map":null,
                    "optMap": {
                        "key1": "",
                        "key2": null,
                        "key3": "3"
                    }
                }
                """
        ).deserializesInto(
                new Data(
                        "s",
                        empty(),
                        List.of(),
                        Set.of(),
                        Map.of(),
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
        assertThatJson("""
                {
                    "s":"s",
                    "optional":null,
                    "list":["1", null],
                    "set":[null, "1"],
                    "map": {
                        "key1": "",
                        "key2": null,
                        "key3": "3"
                    },
                    "optMap": {
                        "opt1": null,
                        "opt2": "",
                        "opt3": "opt3"
                    }
                }
                """
        ).using(b -> b.filterNullValuesInMaps(true).filterNullsInCollections(true))
        .deserializesInto(
                new Data(
                        "s",
                        empty(),
                        List.of("1"),
                        Set.of("1"),
                        Map.of("key3", 3),
                        Map.of("opt1", empty(), "opt2", empty(), "opt3", of("opt3"))
                )
        );
    }

}
