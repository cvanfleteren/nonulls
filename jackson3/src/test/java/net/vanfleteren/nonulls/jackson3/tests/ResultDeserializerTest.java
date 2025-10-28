package net.vanfleteren.nonulls.jackson3.tests;

import lombok.SneakyThrows;
import net.vanfleteren.nonulls.jackson3.api.Result;
import net.vanfleteren.nonulls.jackson3.tests.support.JacksonTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ResultDeserializerTest extends JacksonTest {

    record Data(String s){}

    @Test
    @SneakyThrows
    void result_deserializes_success() {
        String json = """
                {"s":"value"}
                """;

       Result<Data> res =  noNullDefault.readValue(json, new TypeReference<>(){});
       assertThat(res).isInstanceOf(Result.Success.class);

    }

    @Test
    @SneakyThrows
    void result_deserializes_invalidJsonTypes() {
        String json = """
                {"s":{"foo":"value"}}
                """;

        Result<Data> res =  readNonNull(noNullDefault, json, new TypeReference<>(){});

        assertThat(res).isInstanceOf(Result.InvalidJson.class);

    }

    @Test
    @SneakyThrows
    void result_deserializes_invalidJsonStructure() {
        String json = """
                {s:"value"}
                """;

       Result<Data> res =  readNonNull(noNullDefault, json, new TypeReference<>(){});

       assertThat(res).isInstanceOf(Result.InvalidJson.class);
    }

    @Test
    @SneakyThrows
    void result_deserializes_nullContainingJson() {
        String json = """
                {"s":null}
                """;

       Result<Data> res =  readNonNull(noNullDefault, json, new TypeReference<>(){});

       assertThat(res).isInstanceOf(Result.NullsFound.class);
    }

    static <T> @NonNull T readNonNull(ObjectMapper m, String json, TypeReference<T> type) throws Exception {
        return Objects.requireNonNull(m.readValue(json, type));
    }


}
