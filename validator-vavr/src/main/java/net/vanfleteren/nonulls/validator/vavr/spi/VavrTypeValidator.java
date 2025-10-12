package net.vanfleteren.nonulls.validator.vavr.spi;

import io.vavr.Value;
import io.vavr.collection.Map;
import io.vavr.collection.Traversable;
import net.vanfleteren.nonulls.validator.spi.RecursiveValidator;
import net.vanfleteren.nonulls.validator.spi.TypeValidator;

import java.util.List;
import java.util.Set;

public class VavrTypeValidator implements TypeValidator {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandle(Class<?> clazz) {
        return Traversable.class.isAssignableFrom(clazz) || Value.class.isAssignableFrom(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Object obj, String path, Set<Integer> visited, List<String> nullPaths, RecursiveValidator recursiveValidator) {
        switch (obj) {
            // Handle Vavr Map
            case Map<?, ?> map -> { // Vavr Map is a special kind of Traversable
                int index = 0;
                for (var entry : map) {
                    recursiveValidator.validate(entry._1(),
                            path + ".key[" + index + "]", visited, nullPaths);
                    recursiveValidator.validate(entry._2(),
                            path + "[" + entry._1() + "]", visited, nullPaths);
                    index++;
                }
            }
            // Handle other Vavr collections (List, Set, Stream, etc.)
            case Traversable<?> traversable -> {

                int index = 0;
                for (Object item : traversable) {
                    recursiveValidator.validate(item,
                            path + "[" + index + "]", visited, nullPaths);
                    index++;
                }
            }
            // Handle Vavr Value (Option, Try, etc.)
            case Value<?> value -> value.forEach(item -> recursiveValidator.validate(item, path, visited, nullPaths));
            default -> {
                // shouldn't happen, we don't handle other types
                // but this is the safe fallback
                recursiveValidator.validate(obj, path, visited, nullPaths);
            }
        }
    }
}