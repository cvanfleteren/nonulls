package net.vanfleteren.nonulls.validation;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class NullValidatorTest {

    @Test
    void noNullsInSimpleValues() {
        // primitives/wrappers/strings should be fine
        NullValidator.assertNoNulls(42);
        NullValidator.assertNoNulls("hello");
        NullValidator.assertNoNulls(3.14);
        NullValidator.assertNoNulls(BigDecimal.valueOf(100));
        assertTrue(NullValidator.hasNoNulls("ok"));
    }

    @Test
    void hasNoNulls_returnsFalseWhenNullsFound() {
        HashMap<String, String> map = new HashMap<>();
        map.put("ok", null);
        assertFalse(NullValidator.hasNoNulls(map));
    }
}
