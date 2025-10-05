package net.vanfleteren.nonulls.jackson2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;
import java.util.Collection;

final class FilteringCollectionDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {
    private final JsonDeserializer<Object> delegate;
    private final boolean skipEmptyStrings;

    FilteringCollectionDeserializer(JsonDeserializer<Object> delegate) {
        this(delegate, true); // or inject via builder
    }

    FilteringCollectionDeserializer(JsonDeserializer<Object> delegate, boolean skipEmptyStrings) {
        this.delegate = delegate;
        this.skipEmptyStrings = skipEmptyStrings;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Object value = delegate.deserialize(p, ctxt);
        if (!(value instanceof Collection<?> coll)) {
            return value;
        }
        // Remove nulls (and optional empty strings)
        coll.removeIf(e -> e == null || (skipEmptyStrings && e instanceof String s && s.isBlank()));
        return coll;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt, Object intoValue) throws IOException {
        Object value = delegate.deserialize(p, ctxt, intoValue);
        if (value instanceof Collection<?> coll) {
            coll.removeIf(e -> e == null || (skipEmptyStrings && e instanceof String s && s.isBlank()));
        }
        return value;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JsonDeserializer<?> ctxd = delegate;
        if (delegate instanceof ContextualDeserializer cd) {
            ctxd = cd.createContextual(ctxt, property);
        }
        return new FilteringCollectionDeserializer((JsonDeserializer<Object>) ctxd, skipEmptyStrings);
    }

    @Override
    public Object getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return delegate.getNullValue(ctxt);
    }
}