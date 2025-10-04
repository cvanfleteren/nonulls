package net.vanfleteren.nonulls.validation;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NullValidatorTest {

    @Test
    void noNullsInSimpleValues() {
        
        assertEquals(List.of(), NullValidator.findNullPaths(42));
        assertEquals(List.of(), NullValidator.findNullPaths("hello"));
        assertEquals(List.of(), NullValidator.findNullPaths(3.14));
        assertEquals(List.of(), NullValidator.findNullPaths(BigDecimal.valueOf(100)));
        assertEquals(List.of(), NullValidator.findNullPaths("ok"));
    }

    @Test
    void hasNoNulls_returnsFalseWhenNullsFound() {
        HashMap<String, String> map = new HashMap<>();
        map.put("ok", null);

        assertEquals(List.of("root[ok]"), NullValidator.findNullPaths(map));
    }
}
