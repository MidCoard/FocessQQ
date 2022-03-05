package top.focess;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class TestSerialization {

    public static class A implements Serializable {
        private int a;

        public A() {
            this.a = 1;
        }
    }

    public static void main(String[] args) throws IOException {
        run();
        while(true);
    }

    private static void run() throws IOException {
        ByteArrayOutputStream a;
        ObjectOutputStream stream = new ObjectOutputStream( a = new ByteArrayOutputStream());
        stream.writeUnshared(new A());
        stream.close();
        System.out.println(Arrays.toString(a.toByteArray()));

    }
}
