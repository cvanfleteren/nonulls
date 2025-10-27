package net.vanfleteren.nonulls.jackson2.internal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.vanfleteren.nonulls.validator.NullValidator;
import net.vanfleteren.nonulls.validator.NullsFoundException;

import java.io.IOException;

public final class NullValidatingDeserializer<T> extends StdDeserializer<T> implements ResolvableDeserializer {

    private final JsonDeserializer<T> defaultDeserializer;

    public NullValidatingDeserializer(JsonDeserializer<T> defaultDeserializer) {
        super((Class<?>) null);
        this.defaultDeserializer = defaultDeserializer;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        T result = defaultDeserializer.deserialize(p, ctxt);

        try {
            NullValidator.assertNoNulls(result);
        } catch (NullsFoundException e) {
            String message = "Null validation failed: " + e.getMessage();
            throw JsonMappingException.from(p, message, e);
        }

        return result;
    }

    @Override
    public void resolve(DeserializationContext ctxt) throws JsonMappingException {
        if (defaultDeserializer instanceof ResolvableDeserializer) {
            ((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
        }
    }
}
