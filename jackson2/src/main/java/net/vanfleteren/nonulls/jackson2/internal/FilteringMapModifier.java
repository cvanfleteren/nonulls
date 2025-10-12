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
import java.util.Map;
import java.util.Objects;

public final class FilteringMapModifier extends com.fasterxml.jackson.databind.deser.BeanDeserializerModifier {

    public FilteringMapModifier() {
    }

    @Override
    public JsonDeserializer<?> modifyMapDeserializer(DeserializationConfig config,
                                                     MapType type,
                                                     BeanDescription beanDesc,
                                                     JsonDeserializer<?> deserializer) {
        return new FilteringMapDeserializer((JsonDeserializer<Object>) deserializer);
    }

    private static final class FilteringMapDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {
        private final JsonDeserializer<Object> delegate;


        FilteringMapDeserializer(JsonDeserializer<Object> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Object value = delegate.deserialize(p, ctxt);

            if (value instanceof Map<?, ?> map) {
                map.values().removeIf(Objects::isNull);
            }

            return value;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt, Object intoValue) throws IOException {
            Object value = delegate.deserialize(p, ctxt, intoValue);

            if (value instanceof Map<?, ?> map) {
                map.values().removeIf(Objects::isNull);
            }

            return value;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, @Nullable BeanProperty property) throws JsonMappingException {
            JsonDeserializer<?> ctxd = delegate;
            if (delegate instanceof ContextualDeserializer cd) {
                ctxd = cd.createContextual(ctxt, property);
            }
            return new FilteringMapDeserializer((JsonDeserializer<Object>) ctxd);
        }

        @Override
        public Object getNullValue(DeserializationContext ctxt) throws JsonMappingException {
            return delegate.getNullValue(ctxt);
        }
    }


}
