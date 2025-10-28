package net.vanfleteren.nonulls.jackson3.internal;

import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.ValueDeserializerModifier;

public final class NullValidatingDeserializerModifier extends ValueDeserializerModifier {

    @Override
    public ValueDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription.Supplier beanDescRef, ValueDeserializer<?> deserializer) {
        return new NullValidatingDeserializer<>(deserializer);
    }

}
