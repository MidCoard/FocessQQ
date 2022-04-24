package top.focess.qq.test;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.focess.command.CommandArgument;
import top.focess.command.CommandDuplicateException;
import top.focess.command.CommandResult;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandLine;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.test.environment.TestEnvironment;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static top.focess.command.CommandArgument.ofLong;

@DisplayName("Command Test")
public class TestCommand {

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

    @BeforeAll
    static void init() {
        TestEnvironment.setup(new String[]{
                "--user", "123456789", "19283746", "--noDefaultPluginLoad", "--admin", "123456789"
        });
    }

    @AfterAll
    static void exit() {
        FocessQQ.preExit();
    }
}
