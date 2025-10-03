package net.vanfleteren.nonulls.jackson2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Optional;

public class OptionalStringDeserializer extends JsonDeserializer<Optional<String>> {

    @Override
    public Optional<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();

        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(value);
    }
}