package net.vanfleteren.nonulls.jackson2.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.SneakyThrows;
import net.vanfleteren.nonulls.jackson2.NoNullsModule;
import org.assertj.core.api.Assertions;

import java.util.function.Consumer;

public class JsonAssert {

    private ObjectMapper noNullsMapper = new ObjectMapper().registerModules(new Jdk8Module(), new NoNullsModule());

    private String actual;

    JsonAssert(String s) {
        actual = s;
    }

    public static JsonAssert assertThatJson(String json) {
        return new JsonAssert(json);
    }

    public JsonAssert using(Consumer<NoNullsModule.Builder> c) {
        NoNullsModule.Builder builder = new NoNullsModule.Builder();
        c.accept(builder);
        this.noNullsMapper =  new ObjectMapper().registerModules(new Jdk8Module(), builder.build());
        return this;
    }

    @SneakyThrows
    public void deserializesInto(Object expected) {
        Object deser = noNullsMapper.readValue(actual, expected.getClass());
        Assertions.assertThat(deser).isEqualTo(expected);
    }

    @SneakyThrows
    public void failsDeserialisation() {
        Assertions.assertThatExceptionOfType(JsonProcessingException.class).isThrownBy(() -> noNullsMapper.readValue(actual, Object.class));
    }
}
