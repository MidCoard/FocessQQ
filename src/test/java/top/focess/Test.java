package top.focess;

import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.api.serialize.FocessSerializable;
import top.focess.qq.api.util.config.DefaultConfig;
import top.focess.qq.api.util.yaml.YamlLoadException;

import java.io.File;

public class Test {

    public static class A implements FocessSerializable {


        @Nullable
        private final B b;

        A() {
            this.b = null;
        }

        A(B b) {
            this.b = b;
        }

        @EnsuresNonNullIf(result = true, expression = "b")
        boolean isB() {
            return this.b != null;
        }

        @Nullable
        B getB() {
            return this.b;
        }

        @Override
        public String toString() {
            return "A{" +
                    "b=" + b +
                    '}';
        }
    }

    public static class B implements FocessSerializable {

        void c(){
            System.out.println("I am not null");
        }
    }

    public static void main(String[] args) throws YamlLoadException {
        DefaultConfig config = new DefaultConfig(new File("config.yml"));
        DefaultConfig a = config.getSection("a");
//        a.set("null0", null);
//        a.set("null1", "null");
        System.out.println(a.get("null0").toString());
        config.save();
    }
}
