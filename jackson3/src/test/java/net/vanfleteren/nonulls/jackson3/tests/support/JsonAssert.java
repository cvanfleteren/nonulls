package net.vanfleteren.nonulls.jackson3.tests.support;


import lombok.SneakyThrows;
import net.vanfleteren.nonulls.jackson3.api.NoNullsModule;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.function.Consumer;

public class JsonAssert {

    private ObjectMapper noNullsMapper = new ObjectMapper().rebuild().addModule(new NoNullsModule()).build();

    private String actual;
    @Nullable
    private TypeReference<?> typeReference;

    JsonAssert(String s) {
        actual = s;
    }

    public static JsonAssert assertThatJson(String json) {
        return new JsonAssert(json);
    }

    public JsonAssert using(Consumer<NoNullsModule.Builder> c) {
        NoNullsModule.Builder builder = new NoNullsModule.Builder();
        c.accept(builder);
        this.noNullsMapper =  new ObjectMapper().rebuild().addModule(builder.build()).build();
        return this;
    }

    public JsonAssert whenReadAs(TypeReference<?> typeReference) {
        this.typeReference = typeReference;
        return this;
    }
    
    public void deserializesInto(Object expected) {
        if(typeReference != null) {
            Object deser = noNullsMapper.readValue(actual, typeReference);
            Assertions.assertThat(deser).isEqualTo(expected);
        } else {
            Object deser = noNullsMapper.readValue(actual, expected.getClass());
            Assertions.assertThat(deser).isEqualTo(expected);
        }
    }

    public void failsDeserialisation() {
        Assertions.assertThatExceptionOfType(JacksonException.class).isThrownBy(() -> noNullsMapper.readValue(actual, Object.class));
    }
}
