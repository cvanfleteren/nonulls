package net.vanfleteren.nonulls.jackson2;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;

public class EmptyAwareOptionalDeserializer extends JsonDeserializer<Optional<?>>
        implements ContextualDeserializer {

    @Nullable
    private final JavaType valueType;

    public EmptyAwareOptionalDeserializer() {
        this.valueType = null;
    }

    private EmptyAwareOptionalDeserializer(JavaType valueType) {
        this.valueType = valueType;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, @Nullable BeanProperty property)
            throws JsonMappingException {
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
        if (valueType != null && valueType.getRawClass() == String.class) {
            String value = p.getValueAsString();
            if (value == null || value.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(value);
        }

        // For other types, deserialize normally
        if (valueType != null) {
            JsonDeserializer<Object> deser = ctxt.findRootValueDeserializer(valueType);
            Object value = deser.deserialize(p, ctxt);
            return Optional.ofNullable(value);
        }

        return Optional.empty();
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