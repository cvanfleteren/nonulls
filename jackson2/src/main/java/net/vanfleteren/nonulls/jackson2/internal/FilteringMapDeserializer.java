package net.vanfleteren.nonulls.jackson2.internal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public final class FilteringMapDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {
    private final JsonDeserializer<Object> delegate;
    private final boolean skipNullValues;


    FilteringMapDeserializer(JsonDeserializer<Object> delegate, boolean skipNullValues) {
        this.delegate = delegate;
        this.skipNullValues = skipNullValues;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Object value = delegate.deserialize(p, ctxt);
        if (skipNullValues && value instanceof Map<?, ?> map) {
            map.values().removeIf(Objects::isNull);
        }
        return value;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt, Object intoValue) throws IOException {
        Object value = delegate.deserialize(p, ctxt, intoValue);
        if (skipNullValues && value instanceof Map<?, ?> map) {
            map.values().removeIf(Objects::isNull);
        }
        return value;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JsonDeserializer<?> ctxd = delegate;
        if (delegate instanceof ContextualDeserializer cd) {
            ctxd = cd.createContextual(ctxt, property);
        }
        return new FilteringMapDeserializer((JsonDeserializer<Object>) ctxd, skipNullValues);
    }

    @Override
    public Object getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return delegate.getNullValue(ctxt);
    }
}
