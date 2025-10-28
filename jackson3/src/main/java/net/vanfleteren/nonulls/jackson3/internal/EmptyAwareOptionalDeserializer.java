package net.vanfleteren.nonulls.jackson3.internal;


import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;

import java.util.Objects;
import java.util.Optional;

public final class EmptyAwareOptionalDeserializer extends ValueDeserializer<Optional<?>> {

    private final JavaType valueType;

    public EmptyAwareOptionalDeserializer() {
        this.valueType = null;
    }

    private EmptyAwareOptionalDeserializer(JavaType valueType) {
        this.valueType = Objects.requireNonNull(valueType);
    }

    @Override
    public EmptyAwareOptionalDeserializer createContextual(DeserializationContext ctxt, @Nullable BeanProperty property) {
        JavaType wrapperType = property != null ? property.getType() : ctxt.getContextualType();
        JavaType valueType = wrapperType.containedType(0);
        return new EmptyAwareOptionalDeserializer(valueType);
    }

    @Override
    public Optional<?> deserialize(JsonParser p, DeserializationContext ctxt) {
        JsonToken token = p.currentToken();

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
        ValueDeserializer<Object> deser = ctxt.findRootValueDeserializer(valueType);
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
