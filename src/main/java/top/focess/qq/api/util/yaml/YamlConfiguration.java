package top.focess.qq.api.util.yaml;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.exceptions.NotFocessSerializableException;
import top.focess.qq.api.exceptions.SerializationParseException;
import top.focess.qq.api.exceptions.YamlLoadException;
import top.focess.qq.api.serialize.FocessSerializable;
import top.focess.qq.api.util.SectionMap;
import top.focess.qq.core.plugin.PluginCoreClassLoader;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * This class is used to define a YAML configuration.
 */
public class YamlConfiguration implements SectionMap {

    private static final Yaml YAML = new Yaml();
    private final Map<String, Object> values;
    private static final PureJavaReflectionProvider PROVIDER = new PureJavaReflectionProvider();
    private static final Map<Class<?>, ReservedHandler<?>> CLASS_RESERVED_HANDLER_MAP = Maps.newHashMap();

    static {
        CLASS_RESERVED_HANDLER_MAP.put(Class.class, new ReservedHandler<Class>() {
            @Override
            public Object write(Class value) {
                return value.getName();
            }

            @Override
            public Class read(Object value) {
                try {
                    String cls = value.toString();
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
                } catch (ClassNotFoundException e) {
                    throw new SerializationParseException(e);
                }
            }
        });
        CLASS_RESERVED_HANDLER_MAP.put(ArrayList.class, new ReservedHandler<ArrayList>() {
            @Override
            public Object write(ArrayList list) {
                return list.stream().map(YamlConfiguration::write).collect(Collectors.toList());
            }

            @Override
            public ArrayList read(Object value) {
                List list = (List) value;
                ArrayList ret = Lists.newArrayList();;
                for (Object o : list) ret.add(YamlConfiguration.read(o));
                return ret;
            }
        });

        CLASS_RESERVED_HANDLER_MAP.put(HashSet.class, new ReservedHandler<HashSet>() {
            @Override
            public Object write(HashSet set) {
                return set.stream().map(YamlConfiguration::write).collect(Collectors.toList());
            }

            @Override
            public HashSet read(Object value) {
                List list = (List) value;
                HashSet ret = new HashSet();
                for (Object o : list) ret.add(YamlConfiguration.read(o));
                return ret;
            }
        });
        CLASS_RESERVED_HANDLER_MAP.put(ConcurrentHashMap.class, new ReservedHandler<ConcurrentHashMap>() {
            @Override
            public Object write(ConcurrentHashMap map) {
                return map;
            }

            @Override
            public ConcurrentHashMap read(Object value) {
                Map map = (Map) value;
                return new ConcurrentHashMap(map);
            }
        });

        CLASS_RESERVED_HANDLER_MAP.put(ConcurrentHashMap.KeySetView.class, new ReservedHandler<ConcurrentHashMap.KeySetView>() {
            @Override
            public Object write(ConcurrentHashMap.KeySetView set) {
                return set.stream().map(YamlConfiguration::write).collect(Collectors.toList());
            }

            @Override
            public ConcurrentHashMap.KeySetView read(Object value) {
                List list = (List) value;
                ConcurrentHashMap.KeySetView ret = ConcurrentHashMap.newKeySet();
                for (Object o : list) ret.add(YamlConfiguration.read(o));
                return ret;
            }
        });

        CLASS_RESERVED_HANDLER_MAP.put(CopyOnWriteArrayList.class, new ReservedHandler<CopyOnWriteArrayList>() {

            @Override
            public Object write(CopyOnWriteArrayList value) {
                return value.stream().map(YamlConfiguration::write).collect(Collectors.toList());
            }

            @Override
            public CopyOnWriteArrayList read(Object value) {
                List list = (List) value;
                CopyOnWriteArrayList ret = new CopyOnWriteArrayList();
                for (Object o : list) ret.add(YamlConfiguration.read(o));
                return ret;
            }
        });
    }

    /**
     * Initialize the YamlConfiguration with existed key-value pairs or not
     *
     * @param values the YAML configuration key-value pairs
     */
    public YamlConfiguration(@Nullable Map<String, Object> values) {
        this.values = values == null ? Maps.newHashMap() : values;
    }

    /**
     * Load the file as a YAML configuration
     *
     * @param file where to load
     * @return YAML configuration
     */
    public static YamlConfiguration loadFile(File file) {
        try {
            FileReader reader = new FileReader(file);
            YamlConfiguration yamlConfiguration = new YamlConfiguration(YAML.load(reader));
            reader.close();
            return yamlConfiguration;
        } catch (IOException e) {
            throw new YamlLoadException(e);
        }
    }

    public static YamlConfiguration load(InputStream inputStream) {
        return new YamlConfiguration(YAML.load(inputStream));
    }

    @Override
    public YamlConfigurationSection createSection(String key) {
        Map<String,Object> values = Maps.newHashMap();
        this.values.put(key,values);
        return new YamlConfigurationSection(this,values);
    }



    @Override
    public void set(String key, Object value) {
        values.put(key,write(value));
    }

    private static <T> Object write(Object value) {
        if (value == null)
            return null;
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof Float || value instanceof Double || value instanceof Character || value instanceof Boolean || value instanceof String)
            return value;
        if (value.getClass().isArray()) {
            Map<String,Object> ret = Maps.newHashMap();
            ret.put("class","!!" + value.getClass().getComponentType().getName());
            List list = Lists.newArrayList();
            for (int i = 0; i < Array.getLength(value); i++)
                list.add(write(Array.get(value, i)));
            ret.put("value",list);
            ret.put("array",true);
            return ret;
        }
        if (value instanceof FocessSerializable) {
            Map<String,Object> ret = Maps.newHashMap();
            ret.put("class","!!" + value.getClass().getName());
            Map<String,Object> data = ((FocessSerializable) value).serialize();
            if (data != null) {
                ret.put("value",data);
                ret.put("serialize",true);
                return ret;
            }
            data = Maps.newHashMap();
            for (Field field : value.getClass().getDeclaredFields()) {
                if ((field.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) == 0){
                    field.setAccessible(true);
                    try {
                        data.put(field.getName(),write(field.get(value)));
                    } catch (IllegalAccessException e) {
                        throw new NotFocessSerializableException(value.getClass().getName());
                    }
                }
            }
            ret.put("value",data);
            return ret;
        }
        if (CLASS_RESERVED_HANDLER_MAP.containsKey(value.getClass())) {
            Map<String,Object> ret = Maps.newHashMap();
            T t = (T) value;
            ReservedHandler<T> handler = (ReservedHandler<T>) CLASS_RESERVED_HANDLER_MAP.get(value.getClass());
            ret.put("value",handler.write(t));
            ret.put("class","!!" + value.getClass().getName());
            return ret;
        }
        throw new NotFocessSerializableException(value.getClass().getName());
    }

    @Override
    public <T> T get(String key) {
        Object value = SectionMap.super.get(key);
        return (T) read(value);
    }

    private static <T> Object read(Object value) {
        if (value == null)
            return null;
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof Float || value instanceof Double || value instanceof Character || value instanceof Boolean || value instanceof String)
            return value;
        if (value instanceof Map) {
            Map<String,Object> map = (Map<String,Object>) value;
            if (map.containsKey("class") && map.containsKey("value")) {
                String className = map.get("class").toString().substring(2);
                try {
                    Class<?> cls = PluginCoreClassLoader.forName(className);
                    Object v = map.get("value");
                    if (v instanceof List && Boolean.parseBoolean(String.valueOf(map.get("array")))) {
                        List list = (List) v;
                        Object array = Array.newInstance(cls,list.size());
                        for (int i = 0; i < list.size(); i++)
                            Array.set(array,i,read(list.get(i)));
                        return array;
                    }
                    if (v instanceof Map && FocessSerializable.class.isAssignableFrom(cls)) {
                        Map<String,Object> data = (Map<String,Object>) v;
                        if (Boolean.parseBoolean(String.valueOf(map.get("serialize")))) {
                            Method method = cls.getMethod("deserialize", Map.class);
                            return method.invoke(null, data);
                        }
                        Object o = PROVIDER.newInstance(cls);
                        for (String key : data.keySet()) {
                            Field f = cls.getDeclaredField(key);
                            f.setAccessible(true);
                            f.set(o, read(data.get(key)));
                        }
                        return o;
                    }
                    if (CLASS_RESERVED_HANDLER_MAP.containsKey(cls)) {
                        ReservedHandler<T> handler = (ReservedHandler<T>) CLASS_RESERVED_HANDLER_MAP.get(cls);
                        return handler.read(v);
                    }
                } catch (Exception e) {
                    throw new SerializationParseException(e);
                }
            }
        }
        throw new SerializationParseException("Unknown class: " + value.getClass().getName());
    }

    @Override
    public Map<String, Object> getValues() {
        return this.values;
    }

    /**
     * Save the YAML configuration as a file
     *
     * @param file where to save
     */
    public void save(File file) {
        try {
            YAML.dump(this.values, new FileWriter(file));
        } catch (IOException e) {
            FocessQQ.getLogger().thrLang("exception-save-file",e);
        }
    }

    @Override
    public YamlConfigurationSection getSection(String key) {
        if (get(key) instanceof Map)
            return new YamlConfigurationSection(this, get(key));
        else throw new IllegalStateException("This " + key + " is not a valid section.");
    }

    @Override
    public String toString() {
        return values.toString();
    }

    private interface ReservedHandler<T> {

        Object write(T value);

        T read(Object value);
    }
}
