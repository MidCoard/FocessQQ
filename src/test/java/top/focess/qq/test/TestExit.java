package top.focess.qq.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.event.ListenerHandler;
import top.focess.qq.api.event.chat.ConsoleChatEvent;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.test.environment.TestEnvironment;
import top.focess.scheduler.AScheduler;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Exit Test")
public class TestExit {

    @BeforeAll
    static void init() {
        TestEnvironment.setup(new String[]{
                "--user", "123456789", "19283746", "--noDefaultPluginLoad", "--admin", "123456789"
        });
    }

    @Test
    void testExit() {
        FocessQQ.preExit();
        assertEquals(1, AScheduler.getSchedulers().size());
        // why 1, because the scheduler in FocessCallback is not closed
        assertEquals(0, Command.getCommands().size());
        assertEquals(0, Plugin.getPlugins().size());
        Field field = null;
        try {
            field = ConsoleChatEvent.class.getDeclaredField("LISTENER_HANDLER");
            field.setAccessible(true);
        } catch (Exception e) {
            fail();
        }
        Field finalField = field;
        ListenerHandler listenerHandler = (ListenerHandler) assertDoesNotThrow(()-> finalField.get(null));
        assertEquals(0, listenerHandler.size());
    }
}
