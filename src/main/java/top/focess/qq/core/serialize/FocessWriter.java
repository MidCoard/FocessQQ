package top.focess.qq.core.serialize;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Bytes;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.exceptions.NotFocessSerializableException;
import top.focess.qq.api.serialize.FocessSerializable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static top.focess.qq.core.serialize.Opcodes.*;

public class FocessWriter {

    private final List<Byte> data = Lists.newArrayList();

    private static final Map<Class<?>,Writer<?>> CLASS_WRITER_MAP = Maps.newHashMap();

    static {
        CLASS_WRITER_MAP.put(ArrayList.class, (Writer<ArrayList>) (list, writer) -> {
            writer.writeInt(list.size());
            for (Object o : list)
                writer.writeObject(o);
        });
        CLASS_WRITER_MAP.put(LinkedList.class, (Writer<LinkedList>) (linkedList, writer) -> {
            writer.writeInt(linkedList.size());
            for (Object o : linkedList)
                writer.writeObject(o);
        });
        CLASS_WRITER_MAP.put(HashMap.class, (Writer<HashMap>) (hashMap, writer) -> {
            writer.writeInt(hashMap.size());
            for (Object o : hashMap.keySet()) {
                writer.writeObject(o);
                writer.writeObject(hashMap.get(o));
            }
        });
        CLASS_WRITER_MAP.put(TreeSet.class, (Writer<TreeSet>) (set, writer) -> {
            writer.writeInt(set.size());
            for (Object o : set)
                writer.writeObject(o);
        });
        CLASS_WRITER_MAP.put(HashSet.class, (Writer<HashSet>) (set, writer) -> {
            writer.writeInt(set.size());
            for (Object o : set)
                writer.writeObject(o);
        });
        CLASS_WRITER_MAP.put(TreeMap.class, (Writer<TreeMap>) (map, writer) -> {
            writer.writeInt(map.size());
            for (Object o : map.keySet()) {
                writer.writeObject(o);
                writer.writeObject(map.get(o));
            }
        });
        CLASS_WRITER_MAP.put(Class.class, (Writer<Class>) (clazz, writer) -> writer.writeString(clazz.getName()));
    }

    private void writeInt(int v) {
        for (int i = 0; i < 4; i++) {
            data.add((byte) (v & 0xFF));
            v >>>= 8;
        }
    }

    private void writeLong(long v) {
        for (int i = 0; i < 8; i++) {
            data.add((byte) (v & 0xFFL));
            v >>>= 8;
        }
    }

    private void writeString(String v) {
        writeInt(v.length());
        for (byte b : v.getBytes(StandardCharsets.UTF_8))
            data.add(b);
    }

    private void writeFloat(float v) {
        writeInt(Float.floatToIntBits(v));
    }

    private void writeDouble(double v) {
        writeLong(Double.doubleToLongBits(v));
    }

    private void writeShort(short v) {
        for (int i = 0; i < 2; i++) {
            data.add((byte) (v & 0xFF));
            v >>>= 8;
        }
    }

    private void writeBoolean(boolean v) {
        data.add((byte) (v ? 1 : 0));
    }

    private void writeChar(char v) {
        writeShort((short) v);
    }

    private void writeClass(Class<?> cls,boolean isSerializable) {
        if (cls.equals(Byte.class))
            writeByte(C_BYTE);
        else if (cls.equals(Short.class))
            writeByte(C_SHORT);
        else if (cls.equals(Integer.class))
            writeByte(C_INT);
        else if (cls.equals(Long.class))
            writeByte(C_LONG);
        else if (cls.equals(Float.class))
            writeByte(C_FLOAT);
        else if (cls.equals(Double.class))
            writeByte(C_DOUBLE);
        else if (cls.equals(Boolean.class))
            writeByte(C_BOOLEAN);
        else if (cls.equals(Character.class))
            writeByte(C_CHAR);
        else if (cls.equals(String.class))
            writeByte(C_STRING);
        else if (cls.isArray())
            writeByte(C_ARRAY);
        else if (FocessSerializable.class.isAssignableFrom(cls)) {
            if (isSerializable)
                writeByte(C_SERIALIZABLE);
            else writeByte(C_OBJECT);
            writeString(cls.getName());
        } else if (CLASS_WRITER_MAP.containsKey(cls)) {
            writeByte(C_RESERVED);
            writeString(cls.getName());
        }
        else throw new NotFocessSerializableException(cls.getName());
    }

    private <T> void writeObject(Object o) {
        if (o == null) {
            writeByte(C_NULL);
            return;
        }
        boolean isSerializable = o instanceof FocessSerializable;
        Map<String,Object> data = isSerializable ? ((FocessSerializable) o).serialize() : null;
        writeClass(o.getClass(),data != null);
        if (o instanceof Byte)
            writeByte((Byte) o);
        else if (o instanceof Short)
            writeShort((Short) o);
        else if (o instanceof Integer)
            writeInt((Integer) o);
        else if (o instanceof Long)
            writeLong((Long) o);
        else if (o instanceof Float)
            writeFloat((Float) o);
        else if (o instanceof Double)
            writeDouble((Double) o);
        else if (o instanceof Boolean)
            writeBoolean((Boolean) o);
        else if (o instanceof String)
            writeString((String) o);
        else if (o instanceof Character)
            writeChar((Character) o);
        else if (o instanceof FocessSerializable){
            if (data != null)
                writeObject(data);
            else {
                List<Field> fields = Stream.of(o.getClass().getDeclaredFields()).filter(f -> (f.getModifiers() & (Modifier.TRANSIENT | Modifier.STATIC)) == 0).collect(Collectors.toList());
                writeInt(fields.size());
                fields.forEach(f ->{
                    f.setAccessible(true);
                    try {
                        writeField(f.getName(), f.get(o));
                    } catch (IllegalAccessException e) {
                        FocessQQ.getLogger().thrLang("exception-serialize-field", e,f.getName(),o.getClass().getName());
                    }
                });
            }
        } else if (o.getClass().isArray()) {
            writeString(o.getClass().getComponentType().getName());
            int length;
            writeInt(length = Array.getLength(o));
            for (int i = 0; i < length; i++)
                writeObject(Array.get(o, i));
        } else if (CLASS_WRITER_MAP.containsKey(o.getClass())) {
            T t = (T) o;
            Writer<T> writer = (Writer<T>) CLASS_WRITER_MAP.get(o.getClass());
            writer.write(t,this);
        }
        else
            throw new NotFocessSerializableException(o.getClass().getName());
    }

    private void writeField(String name, Object o) {
        this.writeByte(C_FIELD);
        this.writeString(name);
        this.writeObject(o);
    }

    public void write(Object o) {
        writeByte(C_START);
        writeObject(o);
        writeByte(C_END);
    }

    private void writeByte(Byte o) {
        data.add(o);
    }

    public byte[] toByteArray() {
        return Bytes.toArray(this.data);
    }

    private interface Writer<T> {
        void write(T t,FocessWriter writer) throws NotFocessSerializableException;
    }
}
