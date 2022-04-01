package top.focess.qq.core.serialize;

import com.google.common.collect.Maps;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.api.serialize.FocessReader;
import top.focess.qq.api.serialize.SerializationParseException;
import top.focess.qq.core.plugin.PluginCoreClassLoader;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static top.focess.qq.core.serialize.Opcodes.*;

public class SimpleFocessReader extends FocessReader {

    private static final PureJavaReflectionProvider PROVIDER = new PureJavaReflectionProvider();

    private static final Map<Class<?>, Reader<?>> CLASS_READER_MAP = Maps.newHashMap();

    static {
        CLASS_READER_MAP.put(ArrayList.class, (Reader<ArrayList>) (t, reader) -> {
            final ArrayList list = new ArrayList();
            final int length = reader.readInt();
            for (int i = 0; i < length; i++)
                list.add(reader.readObject());
            return list;
        });

        CLASS_READER_MAP.put(LinkedList.class, (Reader<LinkedList>) (t, reader) -> {
            final LinkedList list = new LinkedList();
            final int length = reader.readInt();
            for (int i = 0; i < length; i++)
                list.offer(reader.readObject());
            return list;
        });

        CLASS_READER_MAP.put(HashMap.class, (Reader<HashMap>) (t, reader) -> {
            final HashMap hashMap = new HashMap();
            final int length = reader.readInt();
            for (int i = 0; i < length; i++)
                hashMap.put(reader.readObject(), reader.readObject());
            return hashMap;
        });

        CLASS_READER_MAP.put(TreeMap.class, (Reader<TreeMap>) (t, reader) -> {
            final TreeMap treeMap = new TreeMap();
            final int length = reader.readInt();
            for (int i = 0; i < length; i++)
                treeMap.put(reader.readObject(), reader.readObject());
            return treeMap;
        });

        CLASS_READER_MAP.put(HashSet.class, (Reader<HashSet>) (t, reader) -> {
            final HashSet hashSet = new HashSet();
            final int length = reader.readInt();
            for (int i = 0; i < length; i++)
                hashSet.add(reader.readObject());
            return hashSet;
        });

        CLASS_READER_MAP.put(TreeSet.class, (Reader<TreeSet>) (t, reader) -> {
            final TreeSet treeSet = new TreeSet();
            final int length = reader.readInt();
            for (int i = 0; i < length; i++)
                treeSet.add(reader.readObject());
            return treeSet;
        });

        CLASS_READER_MAP.put(Class.class, (Reader<Class>) (t, reader) -> {
            try {
                final String cls = reader.readString();
                switch (cls) {
                    case "byte":
                        return byte.class;
                    case "short":
                        return short.class;
                    case "int":
                        return int.class;
                    case "long":
                        return long.class;
                    case "float":
                        return float.class;
                    case "double":
                        return double.class;
                    case "boolean":
                        return boolean.class;
                    case "char":
                        return char.class;
                    case "void":
                        return void.class;
                    default:
                        return PluginCoreClassLoader.forName(cls);
                }
            } catch (final ClassNotFoundException e) {
                throw new SerializationParseException(e);
            }
        });

        CLASS_READER_MAP.put(ConcurrentHashMap.KeySetView.class, (Reader<ConcurrentHashMap.KeySetView>) (t, reader) -> {
            final ConcurrentHashMap.KeySetView keySetView = ConcurrentHashMap.newKeySet();
            final int length = reader.readInt();
            for (int i = 0; i < length; i++)
                keySetView.add(reader.readObject());
            return keySetView;
        });
    }

    private final byte[] bytes;

    private int pointer;

    public SimpleFocessReader(final byte[] bytes) {
        this.bytes = bytes;
        this.pointer = 0;
    }

    private int readInt() {
        int r = 0;
        for (int i = 0; i < 4; i++)
            r += (Byte.toUnsignedInt(this.bytes[this.pointer++]) << (i * 8));
        return r;
    }

    private long readLong() {
        long r = 0L;
        for (int i = 0; i < 8; i++)
            r += (Byte.toUnsignedLong(this.bytes[this.pointer++]) << (i * 8L));
        return r;
    }

    private String readString() {
        final int length = this.readInt();
        final byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++)
            bytes[i] = this.bytes[this.pointer++];
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private float readFloat() {
        return Float.intBitsToFloat(this.readInt());
    }

    private double readDouble() {
        return Double.longBitsToDouble(this.readLong());
    }

    private byte readByte() {
        return this.bytes[this.pointer++];
    }

    private short readShort() {
        short r = 0;
        for (int i = 0; i < 2; i++)
            // still the right side is short even if not cast to short
            // because two bytes are used to represent a short
            r += (short) ((short) Byte.toUnsignedInt(this.bytes[this.pointer++]) << (i * 8));
        return r;
    }

    private char readChar() {
        return (char) this.readShort();
    }

    private boolean readBoolean() {
        return this.readByte() == 1;
    }

    @Nullable
    public Object read() {
        if (this.pointer >= this.bytes.length)
            throw new SerializationParseException("Read over");
        final byte start = this.readByte();
        if (start != C_START)
            throw new SerializationParseException("Start code is not correct");
        final Object o = this.readObject();
        final byte end = this.readByte();
        if (end != C_END)
            throw new SerializationParseException("End code is not correct");
        return o;
    }

    private Class<?> readClass() {
        final String cls = this.readString();
        switch (cls) {
            case "byte":
                return byte.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "boolean":
                return boolean.class;
            case "char":
                return char.class;
            case "void":
                return void.class;
            default:
                try {
                    return PluginCoreClassLoader.forName(cls);
                } catch (final ClassNotFoundException e) {
                    throw new SerializationParseException(e);
                }
        }
    }

    @Nullable
    private <T,V extends Enum<V>> Object readObject() {
        final byte type = this.readByte();
        switch (type) {
            case C_NULL:
                return null;
            case C_BYTE:
                return this.readByte();
            case C_SHORT:
                return this.readShort();
            case C_INT:
                return this.readInt();
            case C_LONG:
                return this.readLong();
            case C_FLOAT:
                return this.readFloat();
            case C_DOUBLE:
                return this.readDouble();
            case C_BOOLEAN:
                return this.readBoolean();
            case C_CHAR:
                return this.readChar();
            case C_STRING:
                return this.readString();
            case C_ENUM: {
                try {
                    final Class<V> cls = (Class<V>) this.readClass();
                    return Enum.valueOf(cls, this.readString());
                } catch (final Exception e) {
                    throw new SerializationParseException(e);
                }
            }
            case C_ARRAY: {
                final Class<?> cls = this.readClass();
                final int length = this.readInt();
                final Object array = Array.newInstance(cls, length);
                for (int i = 0; i < length; i++)
                    Array.set(array, i, this.readObject());
                return array;
            }
            case C_SERIALIZABLE: {
                final String className = this.readString();
                final Object o = this.readObject();
                if (o instanceof Map)
                    try {
                        final Class<?> cls = PluginCoreClassLoader.forName(className);
                        final Method method = cls.getMethod("deserialize", Map.class);
                        return method.invoke(null, o);
                    } catch (final Exception e) {
                        throw new SerializationParseException(e);
                    }
                else throw new SerializationParseException("Deserialize argument is not a map");
            }
            case C_OBJECT: {
                final String className = this.readString();
                final int length = this.readInt();
                try {
                    final Class<?> cls = PluginCoreClassLoader.forName(className);
                    final Object o = PROVIDER.newInstance(cls);
                    for (int i = 0; i < length; i++) {
                        final byte field = this.readByte();
                        if (field != C_FIELD)
                            throw new SerializationParseException("Field code is not correct");
                        final String fieldName = this.readString();
                        final Field f = cls.getDeclaredField(fieldName);
                        f.setAccessible(true);
                        f.set(o, this.readObject());
                    }
                    return o;
                } catch (final Exception e) {
                    throw new SerializationParseException(e);
                }
            }
            case C_RESERVED: {
                final String className = this.readString();
                try {
                    final Class<T> cls = (Class<T>) PluginCoreClassLoader.forName(className);
                    final Reader<T> reader;
                    if ((reader = (Reader<T>) CLASS_READER_MAP.get(cls)) != null)
                        return reader.read(cls, this);
                } catch (final ClassNotFoundException e) {
                    throw new SerializationParseException(e);
                }
            }
            default:
                throw new SerializationParseException("Unknown type code");
        }
    }

    private interface Reader<T> {
        T read(Class<T> cls, SimpleFocessReader reader) throws SerializationParseException;
    }

}
