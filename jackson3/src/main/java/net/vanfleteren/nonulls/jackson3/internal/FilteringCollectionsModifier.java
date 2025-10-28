package net.vanfleteren.nonulls.jackson3.internal;


import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.type.*;
import tools.jackson.databind.type.ArrayType;
import tools.jackson.databind.type.CollectionLikeType;
import tools.jackson.databind.type.CollectionType;
import tools.jackson.databind.type.MapType;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

public final class FilteringCollectionsModifier extends ValueDeserializerModifier {

    public FilteringCollectionsModifier() {
    }

    @Override
    public ValueDeserializer<?> modifyArrayDeserializer(DeserializationConfig config, tools.jackson.databind.type.ArrayType valueType, BeanDescription.Supplier beanDescRef, ValueDeserializer<?> deserializer) {
        Class<?> componentClass = valueType.getContentType().getRawClass();
        return new FilteringArrayDeserializer((ValueDeserializer<Object>) deserializer, componentClass);
    }

    @Override
    public ValueDeserializer<?> modifyCollectionDeserializer(DeserializationConfig config, tools.jackson.databind.type.CollectionType type, BeanDescription.Supplier beanDescRef, ValueDeserializer<?> deserializer) {
        return new FilteringCollectionDeserializer((ValueDeserializer<Object>) deserializer, true);
    }

    @Override
    public ValueDeserializer<?> modifyCollectionLikeDeserializer(DeserializationConfig config, tools.jackson.databind.type.CollectionLikeType type, BeanDescription.Supplier beanDescRef, ValueDeserializer<?> deserializer) {
        return new FilteringCollectionDeserializer((ValueDeserializer<Object>) deserializer, true);
    }

    @Override
    public ValueDeserializer<?> modifyMapDeserializer(DeserializationConfig config, tools.jackson.databind.type.MapType type, BeanDescription.Supplier beanDescRef, ValueDeserializer<?> deserializer) {
        return super.modifyMapDeserializer(config, type, beanDescRef, deserializer);
    }

    @Override
    public ValueDeserializer<?> modifyMapLikeDeserializer(DeserializationConfig config, MapLikeType type, BeanDescription.Supplier beanDescRef, ValueDeserializer<?> deserializer) {
        return super.modifyMapLikeDeserializer(config, type, beanDescRef, deserializer);
    }

    @Override
    public KeyDeserializer modifyKeyDeserializer(DeserializationConfig config, JavaType type, KeyDeserializer deserializer) {
        return super.modifyKeyDeserializer(config, type, deserializer);
    }

    private static final class FilteringCollectionDeserializer extends ValueDeserializer<Object>  {
        private final ValueDeserializer<Object> delegate;
        private final boolean skipEmptyStrings;

        FilteringCollectionDeserializer(ValueDeserializer<Object> delegate, boolean skipEmptyStrings) {
            this.delegate = delegate;
            this.skipEmptyStrings = skipEmptyStrings;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) {
            Object value = delegate.deserialize(p, ctxt);
            if (!(value instanceof Collection<?> coll)) {
                return value;
            }
            // Remove nulls (and optional empty strings)
            coll.removeIf(e -> e == null || (skipEmptyStrings && e instanceof String s && s.isBlank()));
            return coll;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt, Object intoValue) {
            Object value = delegate.deserialize(p, ctxt, intoValue);
            if (value instanceof Collection<?> coll) {
                coll.removeIf(e -> e == null || (skipEmptyStrings && e instanceof String s && s.isBlank()));
            }
            return value;
        }

        @Override
        public ValueDeserializer<?> createContextual(DeserializationContext ctxt, @Nullable BeanProperty property) {
            ValueDeserializer<?> ctxd = delegate.createContextual(ctxt, property);
            return new FilteringCollectionDeserializer((ValueDeserializer<Object>) ctxd, skipEmptyStrings);
        }

        @Override
        public Object getNullValue(DeserializationContext ctxt)  {
            return delegate.getNullValue(ctxt);
        }
    }

    private static final class FilteringArrayDeserializer extends ValueDeserializer<Object> {
        private final ValueDeserializer<Object> delegate;
        private final boolean skipEmptyStrings;
        private final Class<?> componentClass;

        FilteringArrayDeserializer(ValueDeserializer<Object> delegate, Class<?> componentClass) { this(delegate, componentClass, true); }
        FilteringArrayDeserializer(ValueDeserializer<Object> delegate, Class<?> componentClass, boolean skipEmptyStrings) {
            this.delegate = delegate; this.componentClass = componentClass; this.skipEmptyStrings = skipEmptyStrings; }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) {
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
            return tmp.toArray((Object[])Array.newInstance(arr.getClass().getComponentType(), tmp.size()));
        }

        @Override
        public ValueDeserializer<?> createContextual(DeserializationContext ctxt, @Nullable BeanProperty property) {
            ValueDeserializer<?> ctxd = delegate.createContextual(ctxt, property);
            return new FilteringArrayDeserializer((ValueDeserializer<Object>) ctxd, componentClass, skipEmptyStrings);
        }

        @Override
        public Object getNullValue(DeserializationContext ctxt) {
            return Array.newInstance(componentClass, 0);
        }
    }
}
