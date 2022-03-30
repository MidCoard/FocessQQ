package top.focess;

import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.api.serialize.FocessSerializable;
import top.focess.qq.api.util.yaml.YamlLoadException;
import top.focess.qq.core.serialize.SimpleFocessReader;
import top.focess.qq.core.serialize.SimpleFocessWriter;

public class Test {

    public enum ABC {
        AB,C;
    }

    public static void main(String[] args) throws YamlLoadException {
        SimpleFocessWriter writer = new SimpleFocessWriter();
        writer.write(ABC.AB);
        SimpleFocessReader reader = new SimpleFocessReader(writer.toByteArray());
        ABC abc = (ABC) reader.read();
        System.out.println(abc);
    }

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

        void c() {
            System.out.println("I am not null");
        }
    }
}
