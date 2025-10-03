package net.vanfleteren.nonulls.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NullValidatorEdgeCasesTest {


    static class SelfRef {
        SelfRef self;
        String ok = "x";
    }


    static class WithStatic {
        static String staticField = null; // should be ignored
        String val = "ok";
    }

    @Test
    void rootObjectNull_reportsRootPath() {
        assertEquals(java.util.List.of("root"), NullValidator.findNullPaths(null));
    }

    @Test
    void circularReference_isHandledWithoutInfiniteRecursion() {
        SelfRef node = new SelfRef();
        node.self = node; // self-reference creates cycle
        
        assertEquals(java.util.List.of(), NullValidator.findNullPaths(node));
    }

    @Test
    void staticFields_areIgnored_evenWhenNull() {
        WithStatic ws = new WithStatic();
        
        assertEquals(java.util.List.of(), NullValidator.findNullPaths(ws));
    }
}
