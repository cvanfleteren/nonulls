package net.vanfleteren.nonulls.jackson3.api;


import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import net.vanfleteren.nonulls.jackson3.internal.*;
import tools.jackson.databind.module.SimpleDeserializers;
import tools.jackson.databind.module.SimpleModule;

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
            this.filterNullsInCollections = this.filterNullValuesInMaps = this.treatNullCollectionsAsEmpty = this.emptyAwareOptional = false;
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
        deserializers.addDeserializer(Result.class, new ResultDeserializer<>());


        if (emptyAwareOptional) {
            deserializers.addDeserializer(Optional.class, new EmptyAwareOptionalDeserializer());
        }

        if (treatNullCollectionsAsEmpty) {
            context.configOverride(Collection.class).setNullHandling(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
            context.configOverride(Iterable.class).setNullHandling(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
            context.configOverride(List.class).setNullHandling(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
            context.configOverride(Map.class).setNullHandling(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
            context.configOverride(Set.class).setNullHandling(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
        }

        if (filterNullInCollections) {
            // Configure to skip null contents for common collection types (not maps)
            context.configOverride(Collection.class).setNullHandling(JsonSetter.Value.forContentNulls(Nulls.SKIP));
            context.configOverride(Iterable.class).setNullHandling(JsonSetter.Value.forContentNulls(Nulls.SKIP));
            context.configOverride(List.class).setNullHandling(JsonSetter.Value.forContentNulls(Nulls.SKIP));
            context.configOverride(Set.class).setNullHandling(JsonSetter.Value.forContentNulls(Nulls.SKIP));
            // Note: we intentionally do NOT set contentNulls=SKIP for Map to avoid removing entries with null values by default.

            // Additionally, register specialized deserializers for List/Set to cover cases (e.g., record creators)
            // where Jackson may not apply config overrides consistently.
            deserializers.addDeserializer(List.class, new NullFilteringListDeserializer());
            deserializers.addDeserializer(Set.class, new NullFilteringSetDeserializer());

            context.addDeserializerModifier(new FilteringCollectionsModifier());

        }

        if(filterNullValuesInMaps) {
            deserializers.addDeserializer(Map.class, new NullFilteringMapDeserializer());
            // this one works for concrete types
            context.addDeserializerModifier(new FilteringMapModifier());
        }

        context.addDeserializers(deserializers);
    }
}
