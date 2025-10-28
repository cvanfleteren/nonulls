package net.vanfleteren.nonulls.jackson3.internal;

import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;

import java.util.ArrayList;
import java.util.List;

public final class NullFilteringListDeserializer extends tools.jackson.databind.deser.std.StdDeserializer<List<?>> {

    private final JavaType elementType;

    public NullFilteringListDeserializer() {
        super(List.class);
        this.elementType = null;
    }

    private NullFilteringListDeserializer(JavaType elementType) {
        super(List.class);
        this.elementType = elementType;
    }

    @Override
    public NullFilteringListDeserializer createContextual(DeserializationContext ctxt, @Nullable BeanProperty property) {
        JavaType wrapperType = property != null ? property.getType() : ctxt.getContextualType();
        JavaType elementType = wrapperType.containedType(0);
        return new NullFilteringListDeserializer(elementType);
    }

    @Override
    public List<?> deserialize(tools.jackson.core.JsonParser p, DeserializationContext ctxt) {
        List<Object> result = new ArrayList<>();

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
    public List<?> getNullValue(DeserializationContext ctxt) {
        return new ArrayList<>();
    }
}
