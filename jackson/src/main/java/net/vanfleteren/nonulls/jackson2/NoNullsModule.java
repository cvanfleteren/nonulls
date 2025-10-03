package net.vanfleteren.nonulls.jackson2;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class NoNullsModule extends SimpleModule {

    private boolean failOnNulls = false;
    private boolean blankOptionalStringsAreAllowed = false;

    public NoNullsModule failOnNulls() {
        this.failOnNulls = true;
        return this;
    }

    public NoNullsModule blankOptionalStringsAreAllowed() {
        this.blankOptionalStringsAreAllowed = false;
        return this;
    }

    public void setupModule(SetupContext context) {
        super.setupModule(context);
        SimpleDeserializers deserializers = new SimpleDeserializers();

        if (!blankOptionalStringsAreAllowed) {
            deserializers.addDeserializer(Optional.class, new EmptyAwareOptionalDeserializer());
        }

        deserializers.addDeserializer(List.class, new NullFilteringListDeserializer());

        context.configOverride(List.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
        context.configOverride(Map.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
        context.configOverride(Set.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));

        context.addDeserializers(deserializers);
        if (failOnNulls) {
            context.addBeanDeserializerModifier(new NullValidatingDeserializerModifier());
        }
    }
}
