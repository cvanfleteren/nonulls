package net.vanfleteren.nonulls.jackson3.internal;

import net.vanfleteren.nonulls.jackson3.api.Result;
import net.vanfleteren.nonulls.validator.NullValidator;
import net.vanfleteren.nonulls.validator.NullsFoundException;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserializer for Result objects.
 * Will deserialize expected values and wrap them in a Result.InvalidJson or Result.NullsFound object if there is invalid JSON or nulls found.
 * @param <T> The type of the contained value.
 */
public final class ResultDeserializer<T> extends StdDeserializer<Result<T>> {

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
    public Result<T> deserialize(JsonParser p, DeserializationContext ctxt) {
        assert elementType != null;
        var elementDeserializer = ctxt.findRootValueDeserializer(elementType);

        try {
            T resultcontent = (T)elementDeserializer.deserialize(p, ctxt);

            return Result.success(NullValidator.assertNoNulls(resultcontent));
        } catch (JacksonException e) {
            return Result.invalidJson(e);
        } catch (NullsFoundException e) {
            return Result.nullsFound(e);
        }
    }

    @Override
    public ResultDeserializer<T> createContextual(DeserializationContext ctxt, @Nullable BeanProperty property) {
        JavaType wrapperType = property != null ? property.getType() : ctxt.getContextualType();
        JavaType elementType = wrapperType.containedType(0);
        return new ResultDeserializer<>(elementType);
    }
}
