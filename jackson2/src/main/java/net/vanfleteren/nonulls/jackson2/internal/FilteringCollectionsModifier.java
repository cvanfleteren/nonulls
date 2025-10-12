package net.vanfleteren.nonulls.jackson2.internal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

public final class FilteringCollectionsModifier extends com.fasterxml.jackson.databind.deser.BeanDeserializerModifier {

    public FilteringCollectionsModifier() {
    }

    @Override
    public JsonDeserializer<?> modifyCollectionDeserializer(DeserializationConfig config,
                                                            CollectionType type,
                                                            BeanDescription beanDesc,
                                                            JsonDeserializer<?> deserializer) {
        return new FilteringCollectionDeserializer((JsonDeserializer<Object>) deserializer);
    }

    @Override
    public JsonDeserializer<?> modifyCollectionLikeDeserializer(DeserializationConfig config,
                                                                CollectionLikeType type,
                                                                BeanDescription beanDesc,
                                                                JsonDeserializer<?> deserializer) {
        return new FilteringCollectionDeserializer((JsonDeserializer<Object>) deserializer);
    }

    @Override
    public JsonDeserializer<?> modifyArrayDeserializer(DeserializationConfig config,
                                                       ArrayType type,
                                                       BeanDescription beanDesc,
                                                       JsonDeserializer<?> deserializer) {
        Class<?> componentClass = type.getContentType().getRawClass();
        return new FilteringArrayDeserializer((JsonDeserializer<Object>) deserializer, componentClass);
    }

    private static final class FilteringCollectionDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {
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
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, @Nullable BeanProperty property) throws JsonMappingException {
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

    private static final class FilteringArrayDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {
        private final JsonDeserializer<Object> delegate;
        private final boolean skipEmptyStrings;
        private final Class<?> componentClass;

        FilteringArrayDeserializer(JsonDeserializer<Object> delegate, Class<?> componentClass) { this(delegate, componentClass, true); }
        FilteringArrayDeserializer(JsonDeserializer<Object> delegate, Class<?> componentClass, boolean skipEmptyStrings) {
            this.delegate = delegate; this.componentClass = componentClass; this.skipEmptyStrings = skipEmptyStrings; }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Object arr = delegate.deserialize(p, ctxt);
            if (arr == null) {
                return Array.newInstance(componentClass, 0);
            }
            if (!arr.getClass().isArray()) return arr;
            int len = Array.getLength(arr);
            ArrayList<Object> tmp = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                Object e = Array.get(arr, i);
                if (e != null && !(skipEmptyStrings && e instanceof String s && s.isBlank())) {
                    tmp.add(e);
                }
            }
            return tmp.toArray((Object[])Array.newInstance(arr.getClass().getComponentType(), tmp.size()));
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, @Nullable BeanProperty property) throws JsonMappingException {
            JsonDeserializer<?> ctxd = delegate;
            if (delegate instanceof ContextualDeserializer cd) {
                ctxd = cd.createContextual(ctxt, property);
            }
            return new FilteringArrayDeserializer((JsonDeserializer<Object>) ctxd, componentClass, skipEmptyStrings);
        }

        @Override
        public Object getNullValue(DeserializationContext ctxt) throws JsonMappingException {
            return Array.newInstance(componentClass, 0);
        }
    }
}
