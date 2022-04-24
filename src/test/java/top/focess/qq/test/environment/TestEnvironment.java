package top.focess.qq.test.environment;

import top.focess.qq.FocessQQ;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class TestEnvironment {

    public static void setup(String[] args) {
        try {
            Field field = FocessQQ.class.getDeclaredField("BOT_MANAGER");
            field.setAccessible(true);
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(null, new TestBotManager());
            FocessQQ.main(args);
        } catch (Exception e) {
            System.err.println("Failed to setup environment");
            throw new RuntimeException(e);
        }
    }

}
