package net.vanfleteren.nonulls.jackson2.internal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class NullFilteringMapDeserializer extends StdDeserializer<Map<?, ?>>
        implements ContextualDeserializer {

    private final JavaType valueType;

    public NullFilteringMapDeserializer() {
        super(Map.class);
        this.valueType = null;
    }

    private NullFilteringMapDeserializer(JavaType valueType) {
        super(Map.class);
        this.valueType = valueType;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, @Nullable BeanProperty property)
            throws JsonMappingException {
        JavaType wrapperType = property != null ? property.getType() : ctxt.getContextualType();
        JavaType valueType = wrapperType.containedType(1);
        return new NullFilteringMapDeserializer(valueType);
    }

    @Override
    public Map<?, ?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        LinkedHashMap<Object, Object> result = new LinkedHashMap<>();

        if (p.getCurrentToken() != JsonToken.START_OBJECT) {
            ctxt.reportWrongTokenException(this, JsonToken.START_OBJECT,
                    "Expected object start");
            return result;
        }

        JsonDeserializer<Object> valueDeserializer = null;
        if (valueType != null) {
            valueDeserializer = ctxt.findRootValueDeserializer(valueType);
        }

        while (p.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = p.getCurrentName();
            p.nextToken();

            if (p.getCurrentToken() == JsonToken.VALUE_NULL && valueType.getRawClass() != Optional.class) {
                continue; // skip null values entirely
            }
            if (p.getCurrentToken() == JsonToken.VALUE_STRING && valueType.getRawClass() != Optional.class) {
                String val = p.getValueAsString();
                if (val == null || val.isBlank()) {
                    continue; // skip empty/blank strings
                }
            }

            Object value = valueDeserializer != null ? valueDeserializer.deserialize(p, ctxt) : p.readValueAs(Object.class);
            if (value != null && !(value instanceof String s && s.isBlank())) {
                result.put(fieldName, value);
            }
        }

        // Also guard: remove nulls if any slipped through
        result.values().removeIf(Objects::isNull);
        return result;
    }

    @Override
    public Map<?, ?> getNullValue(DeserializationContext ctxt) {
        return new LinkedHashMap<>();
    }
}
