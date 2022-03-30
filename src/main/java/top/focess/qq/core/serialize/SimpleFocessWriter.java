package top.focess.qq.core.serialize;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Bytes;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.serialize.NotFocessSerializableException;
import top.focess.qq.api.serialize.FocessSerializable;
import top.focess.qq.api.serialize.FocessWriter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static top.focess.qq.core.serialize.Opcodes.*;

public class SimpleFocessWriter extends FocessWriter {

    private final List<Byte> data = Lists.newArrayList();

    private static final Map<Class<?>,Writer<?>> CLASS_WRITER_MAP = Maps.newHashMap();

    static {
        CLASS_WRITER_MAP.put(ArrayList.class, (Writer<ArrayList>) (list, writer) -> {
            writer.writeInt(list.size());
            for (final Object o : list)
                writer.writeObject(o);
        });
        CLASS_WRITER_MAP.put(LinkedList.class, (Writer<LinkedList>) (linkedList, writer) -> {
            writer.writeInt(linkedList.size());
            for (final Object o : linkedList)
                writer.writeObject(o);
        });
        CLASS_WRITER_MAP.put(HashMap.class, (Writer<HashMap>) (hashMap, writer) -> {
            writer.writeInt(hashMap.size());
            for (final Object o : hashMap.keySet()) {
                writer.writeObject(o);
                writer.writeObject(hashMap.get(o));
            }
        });
        CLASS_WRITER_MAP.put(TreeSet.class, (Writer<TreeSet>) (set, writer) -> {
            writer.writeInt(set.size());
            for (final Object o : set)
                writer.writeObject(o);
        });
        CLASS_WRITER_MAP.put(HashSet.class, (Writer<HashSet>) (set, writer) -> {
            writer.writeInt(set.size());
            for (final Object o : set)
                writer.writeObject(o);
        });
        CLASS_WRITER_MAP.put(TreeMap.class, (Writer<TreeMap>) (map, writer) -> {
            writer.writeInt(map.size());
            for (final Object o : map.keySet()) {
                writer.writeObject(o);
                writer.writeObject(map.get(o));
            }
        });
        CLASS_WRITER_MAP.put(Class.class, (Writer<Class>) (clazz, writer) -> writer.writeString(clazz.getName()));
        CLASS_WRITER_MAP.put(ConcurrentHashMap.KeySetView.class,(Writer<ConcurrentHashMap.KeySetView>) (set, writer)->{
            writer.writeInt(set.size());
            for(final Object o:set)
                writer.writeObject(o);
        });
    }

    private void writeInt(int v) {
        for (int i = 0; i < 4; i++) {
            this.data.add((byte) (v & 0xFF));
            v >>>= 8;
        }
    }

    private void writeLong(long v) {
        for (int i = 0; i < 8; i++) {
            this.data.add((byte) (v & 0xFFL));
            v >>>= 8;
        }
    }

    private void writeString(final String v) {
        final byte[] bytes = v.getBytes(StandardCharsets.UTF_8);
        this.writeInt(bytes.length);
        this.data.addAll(Bytes.asList(bytes));
    }

    private void writeFloat(final float v) {
        this.writeInt(Float.floatToIntBits(v));
    }

    private void writeDouble(final double v) {
        this.writeLong(Double.doubleToLongBits(v));
    }

    private void writeShort(short v) {
        for (int i = 0; i < 2; i++) {
            this.data.add((byte) (v & 0xFF));
            v >>>= 8;
        }
    }

    private void writeBoolean(final boolean v) {
        this.data.add((byte) (v ? 1 : 0));
    }

    private void writeChar(final char v) {
        this.writeShort((short) v);
    }

    private void writeClass(final Class<?> cls, final boolean isSerializable) {
        if (cls.equals(Byte.class))
            this.writeByte(C_BYTE);
        else if (cls.equals(Short.class))
            this.writeByte(C_SHORT);
        else if (cls.equals(Integer.class))
            this.writeByte(C_INT);
        else if (cls.equals(Long.class))
            this.writeByte(C_LONG);
        else if (cls.equals(Float.class))
            this.writeByte(C_FLOAT);
        else if (cls.equals(Double.class))
            this.writeByte(C_DOUBLE);
        else if (cls.equals(Boolean.class))
            this.writeByte(C_BOOLEAN);
        else if (cls.equals(Character.class))
            this.writeByte(C_CHAR);
        else if (cls.equals(String.class))
            this.writeByte(C_STRING);
        else if (cls.isArray())
            this.writeByte(C_ARRAY);
        else if (FocessSerializable.class.isAssignableFrom(cls)) {
            if (isSerializable)
                this.writeByte(C_SERIALIZABLE);
            else this.writeByte(C_OBJECT);
            this.writeString(cls.getName());
        } else if (CLASS_WRITER_MAP.containsKey(cls)) {
            this.writeByte(C_RESERVED);
            this.writeString(cls.getName());
        }
        else throw new NotFocessSerializableException(cls.getName());
    }

    private <T> void writeObject(final Object o) {
        if (o == null) {
            this.writeByte(C_NULL);
            return;
        }
        final boolean isSerializable = o instanceof FocessSerializable;
        final Map<String,Object> data = isSerializable ? ((FocessSerializable) o).serialize() : null;
        this.writeClass(o.getClass(),data != null);
        if (o instanceof Byte)
            this.writeByte((Byte) o);
        else if (o instanceof Short)
            this.writeShort((Short) o);
        else if (o instanceof Integer)
            this.writeInt((Integer) o);
        else if (o instanceof Long)
            this.writeLong((Long) o);
        else if (o instanceof Float)
            this.writeFloat((Float) o);
        else if (o instanceof Double)
            this.writeDouble((Double) o);
        else if (o instanceof Boolean)
            this.writeBoolean((Boolean) o);
        else if (o instanceof String)
            this.writeString((String) o);
        else if (o instanceof Character)
            this.writeChar((Character) o);
        else if (o instanceof FocessSerializable){
            if (data != null)
                this.writeObject(data);
            else {
                final List<Field> fields = Stream.of(o.getClass().getDeclaredFields()).filter(f -> (f.getModifiers() & (Modifier.TRANSIENT | Modifier.STATIC)) == 0).collect(Collectors.toList());
                this.writeInt(fields.size());
                fields.forEach(f ->{
                    f.setAccessible(true);
                    try {
                        this.writeField(f.getName(), f.get(o));
                    } catch (final IllegalAccessException e) {
                        FocessQQ.getLogger().thrLang("exception-serialize-field", e,f.getName(),o.getClass().getName());
                    }
                });
            }
        } else if (o.getClass().isArray()) {
            this.writeString(o.getClass().getComponentType().getName());
            final int length;
            this.writeInt(length = Array.getLength(o));
            for (int i = 0; i < length; i++)
                this.writeObject(Array.get(o, i));
        } else if (CLASS_WRITER_MAP.containsKey(o.getClass())) {
            final T t = (T) o;
            final Writer<T> writer = (Writer<T>) CLASS_WRITER_MAP.get(o.getClass());
            writer.write(t,this);
        }
        else
            throw new NotFocessSerializableException(o.getClass().getName());
    }

    private void writeField(final String name, final Object o) {
        this.writeByte(C_FIELD);
        this.writeString(name);
        this.writeObject(o);
    }

    public void write(final Object o) {
        this.writeByte(C_START);
        this.writeObject(o);
        this.writeByte(C_END);
    }

    private void writeByte(final Byte o) {
        this.data.add(o);
    }

    public byte[] toByteArray() {
        return Bytes.toArray(this.data);
    }

    private interface Writer<T> {
        void write(T t, SimpleFocessWriter writer) throws NotFocessSerializableException;
    }
}
