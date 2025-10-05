package net.vanfleteren.nonulls.jackson2;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import lombok.RequiredArgsConstructor;

public final class FilteringCollectionsModifier extends com.fasterxml.jackson.databind.deser.BeanDeserializerModifier {

    private final boolean filterNullValuesInMaps;

    public FilteringCollectionsModifier(boolean filterNullValuesInMaps) {
        this.filterNullValuesInMaps = filterNullValuesInMaps;
    }

    @Override
    public JsonDeserializer<?> modifyCollectionDeserializer(DeserializationConfig config,
                                                            CollectionType type,
                                                            BeanDescription beanDesc,
                                                            JsonDeserializer<?> deserializer) {
        return new FilteringCollectionDeserializer((JsonDeserializer<Object>) deserializer);
    }

    @Override
    public JsonDeserializer<?> modifyCollectionLikeDeserializer(DeserializationConfig config,
                                                                CollectionLikeType type,
                                                                BeanDescription beanDesc,
                                                                JsonDeserializer<?> deserializer) {
        return new FilteringCollectionDeserializer((JsonDeserializer<Object>) deserializer);
    }

    @Override
    public JsonDeserializer<?> modifyMapDeserializer(DeserializationConfig config,
                                                     MapType type,
                                                     BeanDescription beanDesc,
                                                     JsonDeserializer<?> deserializer) {
        return new FilteringMapDeserializer((JsonDeserializer<Object>) deserializer, filterNullValuesInMaps);
    }

    @Override
    public JsonDeserializer<?> modifyArrayDeserializer(DeserializationConfig config,
                                                       ArrayType type,
                                                       BeanDescription beanDesc,
                                                       JsonDeserializer<?> deserializer) {
        return new FilteringArrayDeserializer((JsonDeserializer<Object>) deserializer);
    }
}