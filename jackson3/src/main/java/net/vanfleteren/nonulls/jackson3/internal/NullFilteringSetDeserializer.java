package net.vanfleteren.nonulls.jackson3.internal;

import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.LinkedHashSet;
import java.util.Set;

public final class NullFilteringSetDeserializer extends StdDeserializer<Set<?>> {

    private final JavaType elementType;

    public NullFilteringSetDeserializer() {
        super(Set.class);
        this.elementType = null;
    }

    private NullFilteringSetDeserializer(JavaType elementType) {
        super(Set.class);
        this.elementType = elementType;
    }

    @Override
    public NullFilteringSetDeserializer createContextual(DeserializationContext ctxt, @Nullable BeanProperty property) {
        JavaType wrapperType = property != null ? property.getType() : ctxt.getContextualType();
        JavaType elementType = wrapperType.containedType(0);
        return new NullFilteringSetDeserializer(elementType);
    }

    @Override
    public Set<?> deserialize(JsonParser p, DeserializationContext ctxt) {
        LinkedHashSet<Object> result = new LinkedHashSet<>();

        if (p.currentToken() != JsonToken.START_ARRAY) {
            ctxt.reportWrongTokenException(this, JsonToken.START_ARRAY, "Expected array start");
            return result;
        }

        ValueDeserializer<Object> elementDeserializer = ctxt.findRootValueDeserializer(elementType);

        while (p.nextToken() != JsonToken.END_ARRAY) {
            if (p.currentToken() == JsonToken.VALUE_NULL) {
                // Skip null values
                continue;
            }

            if (p.currentToken() == JsonToken.VALUE_STRING) {
                // skip empty strings
                String value = p.getValueAsString();
                if (value == null || value.isBlank()) {
                    continue;
                }
            }

            if (elementDeserializer != null) {
                Object element = elementDeserializer.deserialize(p, ctxt);
                result.add(element);
            }
        }

        return result;
    }

    @Override
    public Set<?> getNullValue(DeserializationContext ctxt) {
        return new LinkedHashSet<>();
    }
}
