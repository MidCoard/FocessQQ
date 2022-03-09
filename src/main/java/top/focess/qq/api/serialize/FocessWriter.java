package top.focess.qq.api.serialize;

import top.focess.qq.core.serialize.SimpleFocessWriter;

import java.io.IOException;
import java.io.OutputStream;

public abstract class FocessWriter {

    public abstract void write(Object o);

    public static FocessWriter newFocessWriter(OutputStream outputStream) {
        return new SimpleFocessWriter(){

            @Override
            public void write(Object o) {
                super.write(o);
                try {
                    outputStream.write(this.toByteArray());
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }
}
