package top.focess;

import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.api.command.SpecialArgumentHandler;

public class Test {

    public static class A {


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
    }

    public static class B {


        void c(){
            System.out.println("I am not null");
        }
    }

    public static void main(String[] args) {
        System.out.println(SpecialArgumentHandler.class.isAssignableFrom(SpecialArgumentHandler.class));
    }
}
