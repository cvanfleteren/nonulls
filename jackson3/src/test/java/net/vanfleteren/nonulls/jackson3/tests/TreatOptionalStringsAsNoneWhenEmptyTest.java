package net.vanfleteren.nonulls.jackson3.tests;

import net.vanfleteren.nonulls.jackson3.tests.support.JacksonTest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class TreatOptionalStringsAsNoneWhenEmptyTest extends JacksonTest {

    @Test
    void treat_optionalBlankStrings_asOptionalEmpty() throws Exception {

        record Data(Optional<String> string, Optional<String> string2, Optional<String> string3) {
        }

        String json = """
                {
                    "string": null,
                    "string2": "",
                    "string3": "  "
                }
                """;

        {
            Data jackson = defaultJacksonMapper.readValue(json, Data.class);
            Data noNullsDisabled = noNullDisabled.readValue(json, Data.class);

            assertThat(jackson.string).isEmpty();
            assertThat(jackson.string2).contains("");
            assertThat(jackson.string3).contains("  ");

            assertThat(jackson).isEqualTo(noNullsDisabled);
        }

        {
            Data noNulls = noNullDefault.readValue(json, Data.class);

            // instead of an Optional.of("") we get an Optional.empty() with noNulls
            assertThat(noNulls.string).isEmpty();
            assertThat(noNulls.string2).isEmpty();
            assertThat(noNulls.string3).isEmpty();
        }

    }

}
