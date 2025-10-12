package net.vanfleteren.nonulls.jackson2.tests.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.vanfleteren.nonulls.jackson2.api.NoNullsModule;
import net.vanfleteren.nonulls.validator.NullValidator;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public abstract class JacksonTest {

    // these two should be functionally the same
    protected ObjectMapper defaultJacksonMapper = new ObjectMapper().findAndRegisterModules();
    protected ObjectMapper noNullDisabled = new ObjectMapper().findAndRegisterModules().registerModule(new NoNullsModule.Builder().disableAll().build());

    // this is the behaviour we really want :)
    protected ObjectMapper noNullDefault = new ObjectMapper().registerModule(buildNoNullsModule());

    protected NoNullsModule.Builder noNullsBuilder() {
        return new NoNullsModule.Builder();
    };


    protected NoNullsModule buildNoNullsModule() {
        return new NoNullsModule.Builder().build();
    }

    @SneakyThrows
    public <T> void assertDefaultHasNullsAt(String json, Class<T> type, String... expectedNullPaths) {

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
    public <T> T assertNoNullsHasNoNulls(String json, Class<T> type)  {
        T value = noNullDefault.readValue(json, type);
        List<String> nullPaths = NullValidator.findNullPaths(value);

        assertThat(nullPaths).isEmpty();

        return value;
    }

    @SneakyThrows
    public <T> void assertNoNullsHasNullsAt(String json, Class<T> type, String... expectedNullPaths) {

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
