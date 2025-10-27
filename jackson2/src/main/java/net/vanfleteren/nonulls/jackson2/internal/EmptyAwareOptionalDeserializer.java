package net.vanfleteren.nonulls.jackson2.internal;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public final class EmptyAwareOptionalDeserializer extends JsonDeserializer<Optional<?>>
        implements ContextualDeserializer {

    private final JavaType valueType;

    public EmptyAwareOptionalDeserializer() {
        this.valueType = null;
    }

    private EmptyAwareOptionalDeserializer(JavaType valueType) {
        this.valueType = Objects.requireNonNull(valueType);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, @Nullable BeanProperty property) {
        JavaType wrapperType = property != null ? property.getType() : ctxt.getContextualType();
        JavaType valueType = wrapperType.containedType(0);
        return new EmptyAwareOptionalDeserializer(valueType);
    }

    @Override
    public Optional<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.getCurrentToken();

        // Handle null explicitly
        if (token == JsonToken.VALUE_NULL) {
            return Optional.empty();
        }


        // For String type, check if empty/blank
        if (valueType.getRawClass() == String.class) {
            String value = p.getValueAsString();
            if (value == null || value.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(value);
        }

        // For other types, deserialize normally
        JsonDeserializer<Object> deser = ctxt.findRootValueDeserializer(valueType);
        Object value = deser.deserialize(p, ctxt);
        return Optional.of(value);
    }

    @Override
    public Optional<?> getNullValue(DeserializationContext ctxt) {
        return Optional.empty();
    }

    @Override
    public Object getAbsentValue(DeserializationContext ctxt) {
        return Optional.empty();
    }
}
