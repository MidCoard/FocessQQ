package top.focess.qq.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.plugin.LazyPlugin;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.test.environment.TestEnvironment;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Plugin Test")
public class TestPlugin {

    @Test
    void testPlugin() {
        assertNull(Plugin.thisPlugin());
        assertNotNull(Plugin.plugin());
        assertEquals(Plugin.plugin(), FocessQQ.getMainPlugin());
    }

    @Test
    void testNewPlugin() {
        assertThrows(IllegalArgumentException.class, () -> new LazyPlugin(){});
        assertThrows(IllegalStateException.class, FocessQQ.MainPlugin::new);
    }

    @Test
    void testPluginName() {
        assertEquals("Main", Plugin.plugin().getName());
    }

    @BeforeAll
    static void init() {
        TestEnvironment.setup(new String[]{
                "--user", "123456789", "19283746", "--noDefaultPluginLoad", "--admin", "123456789"
        });
    }

    @AfterAll
    static void exit() {
        FocessQQ.exit();
    }
}
