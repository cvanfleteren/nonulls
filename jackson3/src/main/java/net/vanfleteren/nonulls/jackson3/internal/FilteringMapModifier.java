package net.vanfleteren.nonulls.jackson3.internal;

import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.type.MapLikeType;

import java.util.Map;
import java.util.Objects;

public final class FilteringMapModifier extends ValueDeserializerModifier {

    public FilteringMapModifier() {
    }

    @Override
    public ValueDeserializer<?> modifyMapDeserializer(DeserializationConfig config, tools.jackson.databind.type.MapType type, BeanDescription.Supplier beanDescRef, ValueDeserializer<?> deserializer) {
        return new FilteringMapDeserializer((ValueDeserializer<Object>) deserializer);
    }

    @Override
    public ValueDeserializer<?> modifyMapLikeDeserializer(DeserializationConfig config, MapLikeType type, BeanDescription.Supplier beanDescRef, ValueDeserializer<?> deserializer) {
        return new FilteringMapDeserializer((ValueDeserializer<Object>) deserializer);
    }

    private static final class FilteringMapDeserializer extends ValueDeserializer<Object> {
        private final ValueDeserializer<Object> delegate;


        FilteringMapDeserializer(ValueDeserializer<Object> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) {
            Object value = delegate.deserialize(p, ctxt);

            if (value instanceof Map<?, ?> map) {
                map.values().removeIf(Objects::isNull);
            }

            return value;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt, Object intoValue) {
            Object value = delegate.deserialize(p, ctxt, intoValue);

            if (value instanceof Map<?, ?> map) {
                map.values().removeIf(Objects::isNull);
            }

            return value;
        }

        @Override
        public ValueDeserializer<?> createContextual(DeserializationContext ctxt, @Nullable BeanProperty property) {
            ValueDeserializer<?> ctxd = delegate.createContextual(ctxt, property);

            return new FilteringMapDeserializer((ValueDeserializer<Object>) ctxd);
        }

        @Override
        public Object getNullValue(DeserializationContext ctxt) {
            return delegate.getNullValue(ctxt);
        }
    }
}
