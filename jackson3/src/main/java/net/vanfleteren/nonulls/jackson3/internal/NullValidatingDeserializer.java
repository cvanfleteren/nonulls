package net.vanfleteren.nonulls.jackson3.internal;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;
import net.vanfleteren.nonulls.validator.NullValidator;
import net.vanfleteren.nonulls.validator.NullsFoundException;
import tools.jackson.databind.exc.JsonNodeException;

import java.io.IOException;

public final class NullValidatingDeserializer<T> extends StdDeserializer<T>  {

    private final ValueDeserializer<T> defaultDeserializer;

    public NullValidatingDeserializer(ValueDeserializer<T> defaultDeserializer) {
        super((Class<?>) null);
        this.defaultDeserializer = defaultDeserializer;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) {
        T result = defaultDeserializer.deserialize(p, ctxt);

        try {
            NullValidator.assertNoNulls(result);
        } catch (NullsFoundException e) {
            String message = "Null validation failed: " + e.getMessage();
            throw JsonNodeException.from(p, message, e);
        }

        return result;
    }

}
