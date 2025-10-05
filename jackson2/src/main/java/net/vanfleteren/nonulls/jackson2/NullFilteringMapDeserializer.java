package net.vanfleteren.nonulls.jackson2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class NullFilteringMapDeserializer extends com.fasterxml.jackson.databind.deser.std.StdDeserializer<Map<?,?>>
        implements com.fasterxml.jackson.databind.deser.ContextualDeserializer  {
    private final com.fasterxml.jackson.databind.JavaType keyType;
    private final com.fasterxml.jackson.databind.JavaType valueType;

    NullFilteringMapDeserializer() { super(Map.class); this.keyType = null; this.valueType = null; }
    private NullFilteringMapDeserializer(com.fasterxml.jackson.databind.JavaType keyType,
                                       com.fasterxml.jackson.databind.JavaType valueType) {
        super(Map.class); this.keyType = keyType; this.valueType = valueType; }

    @Override
    public Map<?,?> getNullValue(com.fasterxml.jackson.databind.DeserializationContext ctxt) {
        return new java.util.LinkedHashMap<>();
    }

    @Override
    public Map<?,?> deserialize(com.fasterxml.jackson.core.JsonParser p,
                              com.fasterxml.jackson.databind.DeserializationContext ctxt) throws java.io.IOException {
        // Delegate to the standard deserializer for non-null cases
        com.fasterxml.jackson.databind.JsonDeserializer<Object> delegate =
                ctxt.findRootValueDeserializer(ctxt.getTypeFactory().constructMapType(java.util.LinkedHashMap.class,
                        keyType == null ? ctxt.constructType(Object.class) : keyType,
                        valueType == null ? ctxt.constructType(Object.class) : valueType));
        Object v = delegate.deserialize(p, ctxt);
        return (Map<?,?>) v;
    }

    @Override
    public com.fasterxml.jackson.databind.JsonDeserializer<?> createContextual(
            com.fasterxml.jackson.databind.DeserializationContext ctxt,
            com.fasterxml.jackson.databind.BeanProperty property) throws com.fasterxml.jackson.databind.JsonMappingException {
        com.fasterxml.jackson.databind.JavaType t = property != null ? property.getType() : ctxt.getContextualType();
        return new NullFilteringMapDeserializer(t.containedType(0), t.containedType(1));
    }
}