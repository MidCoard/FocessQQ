package top.focess.qq.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.bot.BotLoginException;
import top.focess.qq.api.event.*;
import top.focess.qq.api.event.bot.BotLoginEvent;
import top.focess.qq.test.environment.TestEnvironment;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Event Test")
public class TestEvent {

    @BeforeAll
    static void init() {
        TestEnvironment.setup(new String[]{
                "--user", "123456789", "19283746", "--noDefaultPluginLoad", "--admin", "123456789"
        });
    }

    static class CustomEvent extends Event {
        private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();
        private final String customKey;

        public CustomEvent(String customKey){
            this.customKey = customKey;
        }
    }

    static class ErrorEvent extends Event {

    }

    @Test
    void testSubmitCustomEvent() {
        AtomicBoolean flag = new AtomicBoolean(false);
        Listener listener = new Listener() {
            @EventHandler
            public void onCustomEvent(CustomEvent event) {
                flag.set(true);
                assertEquals(event.customKey, "customKey");
            }
        };
        ListenerHandler.register(FocessQQ.getMainPlugin(), listener);
        try {
            EventManager.submit(new CustomEvent("customKey"));
        } catch (EventSubmitException e) {
            fail();
        }
        assertTrue(flag.get());
    }

    @Test
    void testSubmitErrorEvent() {
        assertThrows(EventSubmitException.class, () -> EventManager.submit(new ErrorEvent()));
    }

    @Test
    void testBotLoginEvent() {
        AtomicBoolean flag = new AtomicBoolean(false);
        Listener listener = new Listener(){
            @EventHandler
            public void onBotLogin(BotLoginEvent event) {
                flag.set(true);
            }
        };
        ListenerHandler.register(FocessQQ.getMainPlugin(), listener);
        try {
            FocessQQ.getBotManager().loginDirectly(369087L, "123456",FocessQQ.getMainPlugin());
        } catch (BotLoginException e) {
            fail("login failed");
        }
        assertTrue(flag.get());
    }

    @AfterAll
    static void exit() {
        FocessQQ.preExit();
    }
}
