package top.focess;

import com.google.common.collect.Lists;

import java.util.List;

public class TestMain {

    private static List<A> l = Lists.newArrayList();

    public static class A {
        public B b;
    }


    public static class B {
        public A a;
    }

    public static void c() {
        A a = new A();
        l.add(a);
        B b = new B();
        a.b = b;
        b.a = a;
    }

    public static void main(String[] args) {
        c();
        l.clear();
        while(true);
    }
}
