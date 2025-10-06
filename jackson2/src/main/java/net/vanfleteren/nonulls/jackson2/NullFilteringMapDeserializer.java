package net.vanfleteren.nonulls.jackson2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.util.Map;

final class NullFilteringMapDeserializer extends StdDeserializer<Map<?,?>>
        implements ContextualDeserializer  {
    private final JavaType keyType;
    private final JavaType valueType;

    NullFilteringMapDeserializer() { super(Map.class); this.keyType = null; this.valueType = null; }
    private NullFilteringMapDeserializer(JavaType keyType,
                                       JavaType valueType) {
        super(Map.class); this.keyType = keyType; this.valueType = valueType; }

    @Override
    public Map<?,?> getNullValue(DeserializationContext ctxt) {
        return new java.util.LinkedHashMap<>();
    }

    @Override
    public Map<?,?> deserialize(JsonParser p,
                              DeserializationContext ctxt) throws java.io.IOException {
        // Delegate to the standard deserializer for non-null cases
       JsonDeserializer<Object> delegate = ctxt.findRootValueDeserializer(ctxt.getTypeFactory().constructMapType(java.util.LinkedHashMap.class,
                        keyType == null ? ctxt.constructType(Object.class) : keyType,
                        valueType == null ? ctxt.constructType(Object.class) : valueType));
        Object v = delegate.deserialize(p, ctxt);
        return (Map<?,?>) v;
    }

    @Override
    public JsonDeserializer<?> createContextual(
            DeserializationContext ctxt,
            BeanProperty property) throws JsonMappingException {
        JavaType t = property != null ? property.getType() : ctxt.getContextualType();
        return new NullFilteringMapDeserializer(t.containedType(0), t.containedType(1));
    }
}