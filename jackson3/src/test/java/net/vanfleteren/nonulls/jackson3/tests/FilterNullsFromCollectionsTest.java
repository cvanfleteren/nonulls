package net.vanfleteren.nonulls.jackson3.tests;

import net.vanfleteren.nonulls.jackson3.tests.support.JacksonTest;
import org.junit.jupiter.api.Test;

import java.util.*;

import static java.util.Map.entry;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class FilterNullsFromCollectionsTest extends JacksonTest {

    String json = """
                {
                    "list": [null],
                    "map" : { "x": null, "y": "y" },
                    "set" : [null]
                }
                """;

    @Test
    void filter_nulls_from_collections() throws Exception {
        record Data(List<String> list, Map<String, String> map, Set<Integer> set) {
        }

        {
            Data jackson = defaultJacksonMapper.readValue(json, Data.class);
            Data noNullsDisabled = noNullDisabled.readValue(json, Data.class);

            assertThat(jackson.list).containsExactly((String) null);
            assertThat(jackson.map).containsEntry("x", null);
            assertThat(jackson.map).containsEntry("y", "y");
            assertThat(jackson.set).containsExactly((Integer) null);

            assertThat(jackson).isEqualTo(noNullsDisabled);
        }

        {
            Data noNulls = noNullDefault.readValue(json, Data.class);

            assertThat(noNulls.list).isEmpty();
            assertThat(noNulls.map).containsOnly(entry("y","y"));
            assertThat(noNulls.set).isEmpty();
        }
    }

    @Test
    void filter_nulls_from_collections_concreteCollectionTypes() throws Exception {
        record Data(LinkedList<String> list, TreeMap<String, String> map, LinkedHashSet<Integer> set) {
        }


        {
            Data jackson = defaultJacksonMapper.readValue(json, Data.class);
            Data noNullsDisabled = noNullDisabled.readValue(json, Data.class);

            assertThat(jackson.list).containsExactly((String) null);
            assertThat(jackson.set).containsExactly((Integer) null);
            assertThat(jackson.map).containsEntry("x", null);

            assertThat(jackson).isEqualTo(noNullsDisabled);
        }

        {
            Data noNulls = noNullDefault.readValue(json, Data.class);

            assertThat(noNulls.list).isEmpty();
            assertThat(noNulls.set).isEmpty();
            // map entries with null values are also filtered out by default.
            assertThat(noNulls.map).containsOnly(entry("y", "y"));
        }
    }

}
