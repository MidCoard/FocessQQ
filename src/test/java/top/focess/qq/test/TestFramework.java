package top.focess.qq.test;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import top.focess.command.CommandArgument;
import top.focess.command.CommandDuplicateException;
import top.focess.command.CommandResult;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.bot.BotLoginException;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandLine;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.event.*;
import top.focess.qq.api.event.bot.BotLoginEvent;
import top.focess.qq.api.event.chat.ConsoleChatEvent;
import top.focess.qq.api.plugin.LazyPlugin;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.test.environment.TestEnvironment;
import top.focess.scheduler.AScheduler;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static top.focess.command.CommandArgument.ofLong;

@DisplayName("Framework Test")
@TestMethodOrder(FocessMethodOrder.class)
public class TestFramework {

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

    @Test
    void testCommand() {
        try {
            CommandResult result = CommandLine.exec("plugin list").get();
            assertEquals(result, CommandResult.ALLOW);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void testNewErrorCommand() {
        Command command = new Command("test") {

            @Override
            public void init() {

            }

            @Override
            public @NotNull List<String> usage(CommandSender sender) {
                return Lists.newArrayList();
            }
        };
        assertThrows(CommandDuplicateException.class,() -> Command.register(Plugin.plugin(),command));
    }

    @Test
    void testNewCommand() {
        Command command = new Command("test1","test2") {

            @Override
            public void init() {
                this.addExecutor((sender, dataCollection, ioHandler) -> CommandResult.ALLOW);
            }

            @Override
            public @NotNull List<String> usage(CommandSender sender) {
                return Lists.newArrayList();
            }
        };
        assertDoesNotThrow(() -> Command.register(Plugin.plugin(),command));
        try {
            CommandResult commandResult = CommandLine.exec("test1").get();
            assertEquals(commandResult, CommandResult.ALLOW);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void testSession() {
        assertNull(CommandSender.CONSOLE.getSession().get("hello"));
        CommandSender.CONSOLE.getSession().set("hello", "world");
        assertEquals(CommandSender.CONSOLE.getSession().get("hello"), "world");
        Command command = new Command("test3") {

            @Override
            public void init() {
                this.addExecutor((sender, dataCollection, ioHandler) -> {
                    sender.getSession().set("hello", "world2");
                    return CommandResult.ALLOW;
                });
            }

            @Override
            public @NotNull List<String> usage(CommandSender sender) {
                return Lists.newArrayList();
            }
        };
        Command.register(Plugin.plugin(),command);
        try {
            CommandLine.exec("test3").get();
        } catch (Exception e) {
            fail();
        }
        assertEquals(CommandSender.CONSOLE.getSession().get("hello"), "world2");
    }

    @Test
    void testCommandArgument() {
        AtomicBoolean flag = new AtomicBoolean(false);
        Command command = new Command("test4") {

            @Override
            public void init() {
                this.addExecutor((sender, dataCollection, ioHandler) -> CommandResult.REFUSE);
                this.addExecutor((sender, dataCollection, ioHandler) -> CommandResult.ALLOW, CommandArgument.of("test"),CommandArgument.of("test"));
                this.addExecutor((sender, dataCollection, ioHandler) -> CommandResult.ARGS, CommandArgument.of("test"),CommandArgument.of("test"),CommandArgument.ofInt());
                this.addExecutor((sender, dataCollection, ioHandler) -> {
                    if (dataCollection.getInt() != dataCollection.getLong())
                        return CommandResult.COMMAND_REFUSED;
                    return CommandResult.ALLOW;
                }, CommandArgument.of("test"),CommandArgument.of("test"),CommandArgument.ofInt(),ofLong());
            }

            @Override
            public @NotNull List<String> usage(CommandSender sender) {
                flag.set(true);
                return Lists.newArrayList("test4");
            }
        };
        Command.register(Plugin.plugin(),command);
        assertEquals(CommandResult.REFUSE,assertDoesNotThrow(()->CommandLine.exec("test4").get()));
        assertEquals(CommandResult.ALLOW,assertDoesNotThrow(()->CommandLine.exec("test4 test test").get()));
        assertEquals(CommandResult.ARGS_EXECUTED,assertDoesNotThrow(()->CommandLine.exec("test4 test test 1").get()));
        assertTrue(flag.get());
        assertEquals(CommandResult.COMMAND_REFUSED,assertDoesNotThrow(()->CommandLine.exec("test4 test test 1 2").get()));
        assertEquals(CommandResult.ALLOW,assertDoesNotThrow(()->CommandLine.exec("test4 test test 1 1").get()));
    }

    @Test
    void testExit() {
        Field field = null;
        try {
            field = ConsoleChatEvent.class.getDeclaredField("LISTENER_HANDLER");
            field.setAccessible(true);
        } catch (Exception e) {
            fail();
        }
        Field finalField = field;
        ListenerHandler listenerHandler = (ListenerHandler) assertDoesNotThrow(()-> finalField.get(null));
        assertNotEquals(0,listenerHandler.size());
        assertNotEquals(1,AScheduler.getSchedulers().size());
        assertNotEquals(0, Command.getCommands().size());
        assertNotEquals(0, Plugin.getPlugins().size());
        FocessQQ.preExit();
        assertEquals(1, AScheduler.getSchedulers().size());
        // why 1, because the scheduler in FocessCallback is not closed
        assertEquals(0, Command.getCommands().size());
        assertEquals(0, Plugin.getPlugins().size());
        assertEquals(0, listenerHandler.size());
    }

}
