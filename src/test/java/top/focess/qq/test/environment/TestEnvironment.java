package top.focess.qq.test.environment;

import top.focess.qq.FocessQQ;

public class TestEnvironment {

    public static void setup(String[] args) {
        try {
            Class.forName("top.focess.qq.test.environment.TestBotManager");
            FocessQQ.main(args);
        } catch (Exception e) {
            System.err.println("Failed to setup environment");
            throw new RuntimeException(e);
        }
    }

}
