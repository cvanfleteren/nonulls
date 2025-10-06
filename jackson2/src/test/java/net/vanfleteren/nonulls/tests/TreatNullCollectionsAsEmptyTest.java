package net.vanfleteren.nonulls.tests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import net.vanfleteren.nonulls.tests.support.JacksonTest;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Named.named;

public class TreatNullCollectionsAsEmptyTest extends JacksonTest {

    private static Stream<Arguments> provider() {
        String json = """
                {
                    "list": null,
                    "map" : null,
                    "set" : null,
                    "nestedList": [null]
                }
                """;

        return Stream.of(
                Arguments.of(clazz(Record.class), named("json", json)),
                Arguments.of(clazz(RecordWithCreator.class), named("json",json)),
                Arguments.of(clazz(NoSetters.class), named("json",json)),
                Arguments.of(clazz(JavaBean.class), named("json",json)),
                Arguments.of(clazz(PojoWithCreator.class), named("json",json))
        );
    }


    static <T> Named<Class<T>> clazz(Class<T> clazz) {
        return named(clazz.getSimpleName(), clazz);
    }

    @ParameterizedTest
    @MethodSource("provider")
    <T> void test_for_nulls(Class<T> clazz, String json) {

        assertDefaultHasNullsAt(json, clazz, "root.list", "root.map", "root.set", "root.nestedList[0]");
        assertNoNullsHasNoNulls(json, clazz);

    }

    record Record(List<String> list, Map<String, String> map, Set<Integer> set, List<List<String>> nestedList) {
    }

    record RecordWithCreator(List<String> list, Set<Integer> set, Map<String, String> map,
                             List<List<String>> nestedList) {
        @JsonCreator
        RecordWithCreator(@JsonProperty("list") List<String> list, @JsonProperty("set") Set<Integer> set, @JsonProperty("map") Map<String, String> map, @JsonProperty("nestedList") List<List<String>> nestedList) {
            this.list = list;
            this.set = set;
            this.map = map;
            this.nestedList = nestedList;
        }
    }

    @Getter
    public static class NoSetters {
        private List<String> list;
        private Map<String, String> map;
        private Set<Integer> set;
        private List<List<String>> nestedList;
    }

    @Getter
    @Setter
    public static class JavaBean {
        private List<String> list;
        private Map<String, String> map;
        private Set<Integer> set;
        private List<List<String>> nestedList;
    }

    @Getter
    public static class PojoWithCreator {
        private final List<String> list;
        private final Map<String, String> map;
        private final Set<Integer> set;
        private final List<List<String>> nestedList;

        @JsonCreator
        public PojoWithCreator(
                @JsonProperty("list") List<String> list,
                @JsonProperty("map") Map<String, String> map,
                @JsonProperty("set") Set<Integer> set,
                @JsonProperty("nestedList") List<List<String>> nestedList) {
            this.list = list;
            this.map = map;
            this.set = set;
            this.nestedList = nestedList;
        }
    }
}
