package top.focess;

import com.google.common.collect.Lists;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Queue;

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
        Queue<B> queue = Lists.newLinkedList();
        queue.add(new B());
        if (!queue.isEmpty())
            queue.poll().c();
        if (!Test2.QUEUE.isEmpty())
            Test2.QUEUE.poll().c();
    }
}
