package net.vanfleteren.nonulls.jackson3.internal;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class NullFilteringMapDeserializer extends StdDeserializer<Map<?, ?>> {

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
    public NullFilteringMapDeserializer createContextual(DeserializationContext ctxt, @Nullable BeanProperty property) {
        JavaType wrapperType = property != null ? property.getType() : ctxt.getContextualType();
        JavaType valueType = wrapperType.containedType(1);
        return new NullFilteringMapDeserializer(valueType);
    }

    @Override
    public Map<?, ?> deserialize(JsonParser p, DeserializationContext ctxt)  {
        LinkedHashMap<Object, Object> result = new LinkedHashMap<>();

        if (p.currentToken() != JsonToken.START_OBJECT) {
            ctxt.reportWrongTokenException(this, JsonToken.START_OBJECT, "Expected object start");
            return result;
        }

        ValueDeserializer<Object> valueDeserializer = ctxt.findRootValueDeserializer(valueType);

        while (p.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = p.currentName();
            p.nextToken();

            if (p.currentToken() == JsonToken.VALUE_NULL && valueType.getRawClass() != Optional.class) {
                continue; // skip null values entirely
            }
            if (p.currentToken() == JsonToken.VALUE_STRING && valueType.getRawClass() != Optional.class) {
                String val = p.getValueAsString();
                if (val == null || val.isBlank()) {
                    continue; // skip empty/blank strings
                }
            }

            Object value = valueDeserializer != null ? valueDeserializer.deserialize(p, ctxt) : p.readValueAs(Object.class);
            if (!(value instanceof String s && s.isBlank())) {
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
