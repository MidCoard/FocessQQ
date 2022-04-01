package top.focess.qq.api.serialize;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.core.serialize.SimpleFocessWriter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is used to serialize FocessSerializable-Object.
 */
public abstract class FocessWriter {

    /**
     * New a FocessWriter with given output stream
     * @param outputStream the given output stream
     * @return the FocessWriter with given output stream
     *
     * @throws IllegalStateException if the given output stream is not valid
     */
    @NotNull
    @Contract("_ -> new")
    public static FocessWriter newFocessWriter(final OutputStream outputStream) {
        return new SimpleFocessWriter() {

            @Override
            public void write(final Object o) {
                super.write(o);
                try {
                    outputStream.write(this.toByteArray());
                    outputStream.flush();
                    outputStream.close();
                } catch (final IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }

    /**
     * Write object by this writer
     * @param o the object need to be written
     *
     * @throws NotFocessSerializableException if the object is not FocessSerializable
     */
    public abstract void write(Object o);
}
