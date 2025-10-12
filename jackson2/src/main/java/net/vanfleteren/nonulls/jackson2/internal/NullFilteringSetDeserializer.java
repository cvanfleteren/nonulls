package net.vanfleteren.nonulls.jackson2.internal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public final class NullFilteringSetDeserializer extends StdDeserializer<Set<?>>
        implements ContextualDeserializer {

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
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, @Nullable BeanProperty property)
            throws JsonMappingException {
        JavaType wrapperType = property != null ? property.getType() : ctxt.getContextualType();
        JavaType elementType = wrapperType.containedType(0);
        return new NullFilteringSetDeserializer(elementType);
    }

    @Override
    public Set<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        LinkedHashSet<Object> result = new LinkedHashSet<>();

        if (p.getCurrentToken() != JsonToken.START_ARRAY) {
            ctxt.reportWrongTokenException(this, JsonToken.START_ARRAY,
                    "Expected array start");
            return result;
        }

        JsonDeserializer<Object> elementDeserializer = null;
        if (elementType != null) {
            elementDeserializer = ctxt.findRootValueDeserializer(elementType);
        }

        while (p.nextToken() != JsonToken.END_ARRAY) {
            if (p.getCurrentToken() == JsonToken.VALUE_NULL) {
                // Skip null values
                continue;
            }

            if(p.getCurrentToken() == JsonToken.VALUE_STRING) {
                // skip empty strings
                String value = p.getValueAsString();
                if (value == null || value.isBlank()) {
                    continue;
                }
            }

            if (elementDeserializer != null) {
                Object element = elementDeserializer.deserialize(p, ctxt);
                if (element != null) {
                    result.add(element);
                }
            }
        }

        return result;
    }

    @Override
    public Set<?> getNullValue(DeserializationContext ctxt) {
        return new LinkedHashSet<>();
    }
}
