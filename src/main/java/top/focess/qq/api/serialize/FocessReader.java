package top.focess.qq.api.serialize;

import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;
import top.focess.qq.core.serialize.SimpleFocessReader;

import java.io.InputStream;
import java.util.List;

public abstract class FocessReader {

    public abstract Object read();

    public static FocessReader newFocessReader(final InputStream inputStream) {
        final List<Byte> byteList = Lists.newArrayList();
        final byte[] bytes = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(bytes)) != -1)
                for (int i = 0; i < len; i++)
                    byteList.add(bytes[i]);
            inputStream.close();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
        return new SimpleFocessReader(Bytes.toArray(byteList));
    }
}
