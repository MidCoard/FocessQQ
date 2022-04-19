package top.focess;

import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import top.focess.qq.api.plugin.Plugin;

public class Test2 {

    private static final PureJavaReflectionProvider PROVIDER = new PureJavaReflectionProvider();


    public static class A extends Plugin {

        public A(String s){

        }

        @Override
        public void enable() {

        }

        @Override
        public void disable() {

        }

        @Override
        public String toString() {
            return "Hello";
        }
    }

    public static void main(String[] args) {
        System.out.println(PROVIDER.newInstance(A.class));
    }
}
