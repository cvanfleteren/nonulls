package net.vanfleteren.nonulls.jackson2;

import net.vanfleteren.nonulls.jackson2.support.JacksonTest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class TreatOptionalStringsAsNoneWhenEmptyTest extends JacksonTest {

    @Test
    void treat_optionalEmptyStrings_asOptionalEmpty() throws Exception {

        record Data(Optional<String> string, Optional<String> string2) {
        }

        String json = """
                {
                    "string": null,
                    "string2": ""
                }
                """;

        {
            Data jackson = defaultJacksonMapper.readValue(json, Data.class);
            Data noNullsDisabled = noNullDisabled.readValue(json, Data.class);

            assertThat(jackson.string).isEmpty();
            assertThat(jackson.string2).contains("");

            assertThat(jackson).isEqualTo(noNullsDisabled);
        }

        {
            Data noNulls = noNullDefault.readValue(json, Data.class);

            // instead of an Optional.of("") we get an Optional.empty() with noNulls
            assertThat(noNulls.string).isEmpty();
            assertThat(noNulls.string2).isEmpty();
        }

    }

}
