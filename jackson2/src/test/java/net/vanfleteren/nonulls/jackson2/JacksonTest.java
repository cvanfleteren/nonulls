package net.vanfleteren.nonulls.jackson2;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.vanfleteren.nonulls.validation.NullValidator;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

abstract class JacksonTest {

    // these two should be functionally the same
    ObjectMapper defaultJacksonMapper = new ObjectMapper().findAndRegisterModules();
    ObjectMapper noNullDisabled = new ObjectMapper().findAndRegisterModules().registerModule(new NoNullsModule.Builder().disableAll().build());

    // this is the behaviour we really want :)
    ObjectMapper noNullDefault = new ObjectMapper().registerModule(buildNoNullsModule());


    NoNullsModule buildNoNullsModule() {
        return new NoNullsModule.Builder().build();
    }

    @SneakyThrows
    <T> void assertDefaultHasNullsAt(String json, Class<T> type, String... expectedNullPaths) {

        {
            T value = defaultJacksonMapper.readValue(json, type);
            List<String> nullPaths = NullValidator.findNullPaths(value);

            assertThat(nullPaths).containsExactlyInAnyOrder(expectedNullPaths);
        }

        {
            T value = noNullDisabled.readValue(json, type);
            List<String> nullPaths = NullValidator.findNullPaths(value);

            assertThat(nullPaths).containsExactlyInAnyOrder(expectedNullPaths);
        }
    }

    @SneakyThrows
    <T> T assertNoNullsHasNoNulls(String json, Class<T> type)  {
        T value = noNullDefault.readValue(json, type);
        List<String> nullPaths = NullValidator.findNullPaths(value);

        assertThat(nullPaths).isEmpty();

        return value;
    }

    @SneakyThrows
    <T> void assertNoNullsHasNullsAt(String json, Class<T> type, String... expectedNullPaths) {

        {
            T value = defaultJacksonMapper.readValue(json, type);
            List<String> nullPaths = NullValidator.findNullPaths(value);

            assertThat(nullPaths).containsExactlyInAnyOrder(expectedNullPaths);
        }

        {
            T value = noNullDisabled.readValue(json, type);
            List<String> nullPaths = NullValidator.findNullPaths(value);

            assertThat(nullPaths).containsExactlyInAnyOrder(expectedNullPaths);
        }
    }
}
