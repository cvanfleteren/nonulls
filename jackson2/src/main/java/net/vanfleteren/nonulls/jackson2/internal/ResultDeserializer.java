package net.vanfleteren.nonulls.jackson2.internal;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.vanfleteren.nonulls.jackson2.api.Result;
import net.vanfleteren.nonulls.validation.NullValidator;
import net.vanfleteren.nonulls.validation.NullsFoundException;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Deserializer for Result objects.
 * Will deserialize expected values and wrap them in a Result.InvalidJson or Result.NullsFound object if there is invalid JSON or nulls found.
 * @param <T> The type of the contained value.
 */
public final class ResultDeserializer<T> extends StdDeserializer<Result<T>> implements ContextualDeserializer {

    @Nullable
    private final JavaType elementType;

    public ResultDeserializer(JavaType elementType) {
        super(Result.class);
        this.elementType = elementType;
    }

    public ResultDeserializer() {
        super(Result.class);
        this.elementType = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<T> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        assert elementType != null;
        var elementDeserializer = ctxt.findRootValueDeserializer(elementType);

        try {
            T resultcontent = (T)elementDeserializer.deserialize(p, ctxt);

            return Result.success(NullValidator.assertNoNulls(resultcontent));
        } catch (JsonProcessingException e) {
            return Result.invalidJson(e);
        } catch (NullsFoundException e) {
            return Result.nullsFound(e);
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, @Nullable BeanProperty property) throws JsonMappingException {
        JavaType wrapperType = property != null ? property.getType() : ctxt.getContextualType();
        JavaType elementType = wrapperType.containedType(0);
        return new ResultDeserializer<>(elementType);
    }
}
