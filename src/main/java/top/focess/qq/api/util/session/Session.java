package top.focess.qq.api.util.session;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.util.SectionMap;
import top.focess.qq.core.plugin.PluginCoreClassLoader;
import top.focess.qq.core.util.MethodCaller;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * This class is used to build better communication between Command and CommandSender. It can save something in the executing process and can be used for future.
 */
public class Session implements SectionMap {

    private final Map<String, Object> values;

    /**
     * Initialize the YamlConfiguration with existed key-value pairs or not (usually not)
     *
     * @param values the session key-value pairs
     */
    public Session(@Nullable Map<String,Object> values) {
        this.values = values == null ? Maps.newHashMap() : values;
    }

    @Override
    public SessionSection createSection(String key) {
        Map<String,Object> values = Maps.newHashMap();
        this.set(key,values);
        return new SessionSection(this,values);
    }

    @Override
    public Map<String, Object> getValues() {
        return this.values;
    }

    @Override
    public SectionMap getSection(String key) {
        if (get(key) instanceof  Map)
            return new SessionSection(this,get(key));
        else throw new IllegalStateException("This " + key + " is not a valid section.");
    }

    @Override
    public String toString() {
        return values.toString();
    }

    @Override
    public void set(String key, Object value) {
        Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
        if (plugin != null)
            SectionMap.super.set(plugin.getName() + ":" + key, value);
        else SectionMap.super.set(key, value);
    }

    @Override
    public <T> T get(String key) {
        Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
        if (plugin != null)
            return SectionMap.super.get(plugin.getName() + ":" + key);
        else return SectionMap.super.get(key);
    }

    @Override
    public <T> T getOrDefault(String key, T defaultValue) {
        Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
        if (plugin != null)
            return SectionMap.super.getOrDefault(plugin.getName() + ":" + key, defaultValue);
        else return SectionMap.super.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean contains(String key) {
        Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
        if (plugin != null)
            return SectionMap.super.contains(plugin.getName() + ":" + key);
        else return SectionMap.super.contains(key);
    }

    @Override
    public void remove(String key) {
        Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
        if (plugin != null)
            SectionMap.super.remove(plugin.getName() + ":" + key);
        else SectionMap.super.remove(key);
    }

    @Override
    public void compute(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
        if (plugin != null)
            SectionMap.super.compute(plugin.getName() + ":" + key, remappingFunction);
        else SectionMap.super.compute(key, remappingFunction);
    }
}
