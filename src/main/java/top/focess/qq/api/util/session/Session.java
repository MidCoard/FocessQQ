package top.focess.qq.api.util.session;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.plugin.PluginCoreClassLoader;
import top.focess.qq.core.util.MethodCaller;
import top.focess.util.SectionMap;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * This class is used to build better communication between Command and CommandSender. It can save something in the executing process and can be used for future.
 */
public class Session implements SectionMap {

    private final Map<String, Object> values;

    /**
     * Initializes the Session with existed key-value pairs (usually not)
     *
     * @param values the session key-value pairs
     */
    public Session(@Nullable final Map<String, Object> values) {
        this.values = values == null ? Maps.newHashMap() : values;
    }

    @Override
    public SessionSection createSection(final String key) {
        final Map<String, Object> values = Maps.newHashMap();
        this.set(key, values);
        return new SessionSection(this, values);
    }

    @Override
    public Map<String, Object> getValues() {
        return this.values;
    }

    @Override
    public SectionMap getSection(final String key) {
        final Object value = this.get(key);
        if (value == null)
            this.containsSection(key);
        if (value instanceof Map)
            return new SessionSection(this, (Map<String, Object>) value);
        throw new IllegalStateException("This " + key + " is not a valid section.");
    }

    @Override
    public boolean containsSection(final String key) {
        return this.get(key) instanceof Map;
    }

    @Override
    public String toString() {
        return this.values.toString();
    }

    @Override
    public void set(final String key, final Object value) {
        final Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
        if (plugin != null)
            SectionMap.super.set(plugin.getName() + ":" + key, value);
        else SectionMap.super.set(key, value);
    }

    @Override
    public <T> T get(final String key) {
        final Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
        if (plugin != null)
            return SectionMap.super.get(plugin.getName() + ":" + key);
        else return SectionMap.super.get(key);
    }

    @Override
    public <T> T getOrDefault(final String key, final T defaultValue) {
        final Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
        if (plugin != null)
            return SectionMap.super.getOrDefault(plugin.getName() + ":" + key, defaultValue);
        else return SectionMap.super.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean contains(final String key) {
        final Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
        if (plugin != null)
            return SectionMap.super.contains(plugin.getName() + ":" + key);
        else return SectionMap.super.contains(key);
    }

    @Override
    public void remove(final String key) {
        final Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
        if (plugin != null)
            SectionMap.super.remove(plugin.getName() + ":" + key);
        else SectionMap.super.remove(key);
    }

    @Override
    public void compute(final String key, final BiFunction<? super String, ? super Object, ?> remappingFunction) {
        final Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
        if (plugin != null)
            SectionMap.super.compute(plugin.getName() + ":" + key, remappingFunction);
        else SectionMap.super.compute(key, remappingFunction);
    }
}
