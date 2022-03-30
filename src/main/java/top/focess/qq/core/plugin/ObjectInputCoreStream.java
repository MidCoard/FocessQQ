package top.focess.qq.core.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class ObjectInputCoreStream extends ObjectInputStream {

    public ObjectInputCoreStream(final InputStream inputStream) throws IOException {
        super(inputStream);
    }

    @Override
    protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        try {
            return super.resolveClass(desc);
        } catch (final ClassNotFoundException e) {
            return PluginCoreClassLoader.forName(desc.getName());
        }
    }
}