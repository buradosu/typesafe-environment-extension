import com.github.buradosu.config.EnvironmentConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

class EnvironmentConfigTest {

    @Test
    @SetEnvironmentVariable(
            key = "TEST_PREFIX_VAR_ONE",
            value = "Hello, World!"
    )
    void loadConfigTest() {
        EnvironmentConfig configFromEnvironment = new EnvironmentConfig("TEST_PREFIX_");
        Config config = configFromEnvironment.getConfig();
        ConfigObject object = config.getObject("var");
        Assertions.assertNotNull(object);
        String helloWorld = object.toConfig().getString("one");
        Assertions.assertEquals("Hello, World!", helloWorld);
    }
}
