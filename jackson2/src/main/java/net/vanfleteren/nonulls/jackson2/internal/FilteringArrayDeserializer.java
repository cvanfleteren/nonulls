package net.vanfleteren.nonulls.jackson2.internal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public final class FilteringArrayDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {
    private final JsonDeserializer<Object> delegate;
    private final boolean skipEmptyStrings;

    FilteringArrayDeserializer(JsonDeserializer<Object> delegate) { this(delegate, true); }
    FilteringArrayDeserializer(JsonDeserializer<Object> delegate, boolean skipEmptyStrings) {
        this.delegate = delegate; this.skipEmptyStrings = skipEmptyStrings; }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Object arr = delegate.deserialize(p, ctxt);
        if (!arr.getClass().isArray()) return arr;
        int len = Array.getLength(arr);
        ArrayList<Object> tmp = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            Object e = Array.get(arr, i);
            if (e != null && !(skipEmptyStrings && e instanceof String s && s.isBlank())) {
                tmp.add(e);
            }
        }
        Object newArr = Array.newInstance(arr.getClass().getComponentType(), tmp.size());
        for (int i = 0; i < tmp.size(); i++) Array.set(newArr, i, tmp.get(i));
        return newArr;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JsonDeserializer<?> ctxd = delegate;
        if (delegate instanceof ContextualDeserializer cd) {
            ctxd = cd.createContextual(ctxt, property);
        }
        return new FilteringArrayDeserializer((JsonDeserializer<Object>) ctxd, skipEmptyStrings);
    }
}
