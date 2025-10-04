package net.vanfleteren.nonulls.validation.vavr.spi;

import io.vavr.Value;
import io.vavr.collection.Map;
import io.vavr.collection.Traversable;
import net.vanfleteren.nonulls.validation.spi.RecursiveValidator;
import net.vanfleteren.nonulls.validation.spi.TypeValidator;

import java.util.List;
import java.util.Set;

public class VavrTypeValidator implements TypeValidator {

    @Override
    public boolean canHandle(Class<?> clazz) {
        return Traversable.class.isAssignableFrom(clazz) || Value.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object obj, String path, Set<Integer> visited,
                         List<String> nullPaths, RecursiveValidator recursiveValidator) {
        switch (obj) {
            case Map<?, ?> map -> {
                // Handle Vavr Map
                int index = 0;
                for (var entry : map) {
                    recursiveValidator.validate(entry._1(),
                            path + ".key[" + index + "]", visited, nullPaths);
                    recursiveValidator.validate(entry._2(),
                            path + "[" + entry._1() + "]", visited, nullPaths);
                    index++;
                }
            }
            case Traversable<?> traversable -> {
                // Handle other Vavr collections (List, Set, Stream, etc.)
                int index = 0;
                for (Object item : traversable) {
                    recursiveValidator.validate(item,
                            path + "[" + index + "]", visited, nullPaths);
                    index++;
                }
            }
            case Value<?> value ->
                // Handle Vavr Value (Option, Try, etc.)
                    value.forEach(item -> recursiveValidator.validate(item, path, visited, nullPaths));
            default -> {
                // shouldn't happen, we don't handle other types
                // but this is the safe fallback
                recursiveValidator.validate(obj, path, visited, nullPaths);
            }
        }
    }
}