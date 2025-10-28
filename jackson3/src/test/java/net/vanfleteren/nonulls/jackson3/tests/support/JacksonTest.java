package net.vanfleteren.nonulls.jackson3.tests.support;

import lombok.SneakyThrows;
import net.vanfleteren.nonulls.jackson3.api.NoNullsModule;
import net.vanfleteren.nonulls.validator.NullValidator;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public abstract class JacksonTest {

    // these two should be functionally the same
    protected ObjectMapper defaultJacksonMapper = JsonMapper.builder().findAndAddModules().build();
    protected ObjectMapper noNullDisabled =JsonMapper.builder().findAndAddModules().addModule(new NoNullsModule.Builder().disableAll().build()).build();

    // this is the behaviour we really want :)
    protected ObjectMapper noNullDefault = JsonMapper.builder().findAndAddModules()
            .configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS,false)
            .addModule(new NoNullsModule.Builder().build()).build();

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
