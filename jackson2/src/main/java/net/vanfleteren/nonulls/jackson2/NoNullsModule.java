package net.vanfleteren.nonulls.jackson2;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.*;

public final class NoNullsModule extends SimpleModule {

    public static final class Builder {
        boolean treatNullCollectionsAsEmpty = true;
        boolean filterNullsInCollections = true;
        boolean filterNullValuesInMaps = true;

        boolean emptyAwareOptional = true;
        boolean failOnNulls = false;

        public Builder filterNullsInCollections(boolean v) {
            this.filterNullsInCollections = v;
            return this;
        }

        public Builder filterNullValuesInMaps(boolean v) {
            this.filterNullValuesInMaps = v;
            return this;
        }

        public Builder treatNullCollectionsAsEmpty(boolean v) {
            this.treatNullCollectionsAsEmpty = v;
            return this;
        }

        public Builder emptyAwareOptional(boolean v) {
            this.emptyAwareOptional = v;
            return this;
        }

        public Builder failOnNulls(boolean v) {
            this.failOnNulls = v;
            return this;
        }

        public Builder disableAll() {
            this.filterNullsInCollections = this.treatNullCollectionsAsEmpty = this.emptyAwareOptional = false;
            return this;
        }

        public NoNullsModule build() {
            return new NoNullsModule(this);
        }
    }

    private final boolean filterNullInCollections;
    private final boolean filterNullValuesInMaps;
    private final boolean treatNullCollectionsAsEmpty;
    private final boolean emptyAwareOptional;
    private final boolean failOnNulls;

    public static Builder builder() {
        return new Builder();
    }

    public NoNullsModule() {
        this(builder());
    }

    private NoNullsModule(Builder b) {
        this.filterNullInCollections = b.filterNullsInCollections;
        this.filterNullValuesInMaps = b.filterNullValuesInMaps;
        this.treatNullCollectionsAsEmpty = b.treatNullCollectionsAsEmpty;
        this.emptyAwareOptional = b.emptyAwareOptional;
        this.failOnNulls = b.failOnNulls;
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        SimpleDeserializers deserializers = new SimpleDeserializers();

        if (emptyAwareOptional) {
            deserializers.addDeserializer(Optional.class, new EmptyAwareOptionalDeserializer());
        }


        if (filterNullInCollections) {
            // Configure to skip null contents for common collection types (not maps)
            context.configOverride(Collection.class).setSetterInfo(JsonSetter.Value.forContentNulls(Nulls.SKIP));
            context.configOverride(Collection.class).setSetterInfo(JsonSetter.Value.forContentNulls(Nulls.SKIP));
            context.configOverride(Iterable.class).setSetterInfo(JsonSetter.Value.forContentNulls(Nulls.SKIP));
            context.configOverride(List.class).setSetterInfo(JsonSetter.Value.forContentNulls(Nulls.SKIP));
            context.configOverride(Set.class).setSetterInfo(JsonSetter.Value.forContentNulls(Nulls.SKIP));
            // Note: we intentionally do NOT set contentNulls=SKIP for Map to avoid removing entries with null values by default.

            // Additionally, register specialized deserializers for List/Set to cover cases (e.g., record creators)
            // where Jackson may not apply config overrides consistently.
            deserializers.addDeserializer(List.class, new NullFilteringListDeserializer());
            deserializers.addDeserializer(Set.class, new NullFilteringSetDeserializer());
            deserializers.addDeserializer(Map.class, new NullFilteringMapDeserializer());

            context.addBeanDeserializerModifier(new FilteringCollectionsModifier(filterNullValuesInMaps));
        }

        if (treatNullCollectionsAsEmpty) {
            context.configOverride(Collection.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
            context.configOverride(Iterable.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
            context.configOverride(List.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
            context.configOverride(Map.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
            context.configOverride(Set.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
        }

        context.addDeserializers(deserializers);
        if (failOnNulls) {
            context.addBeanDeserializerModifier(new NullValidatingDeserializerModifier());
        }
    }
}
