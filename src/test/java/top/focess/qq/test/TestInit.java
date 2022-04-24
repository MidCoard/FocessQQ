package top.focess.qq.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.focess.qq.FocessQQ;
import top.focess.qq.test.environment.TestEnvironment;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Init Test")
public class TestInit {

    @BeforeAll
    static void init() {
        TestEnvironment.setup(new String[]{
                "--user", "123456789", "19283746", "--noDefaultPluginLoad", "--admin", "123456789"
        });
    }

    @Test
    void testInit() {
        assertEquals(FocessQQ.getBot().getId(), 123456789L);
        assertTrue(FocessQQ.getBot().isAdministrator());
    }

    @AfterAll
    static void exit() {
        FocessQQ.exit();
    }


}
