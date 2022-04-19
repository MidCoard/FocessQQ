package top.focess.qq.api.plugin;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.util.version.Version;
import top.focess.util.yaml.YamlConfiguration;

import java.util.List;

/**
 * The description of plugin.
 */
public class PluginDescription {

    /**
     * The plugin class path
     */
    private final String main;

    /**
     * The plugin dependencies
     */
    private final List<String> dependencies;

    /**
     * The plugin soft dependencies
     */
    private final List<String> softDependencies;

    /**
     * The author name
     */
    private final String author;

    /**
     * The plugin version
     */
    private final Version version;
    /**
     * The plugin name
     */
    private final String name;

    /**
     * Constructs a new PluginDescription from the plugin config
     *
     * @param pluginConfig the plugin config named "plugin.yml"
     */
    public PluginDescription(@NotNull final YamlConfiguration pluginConfig) {
        this.main = pluginConfig.get("main");
        this.author = pluginConfig.getOrDefault("author","");
        this.dependencies = pluginConfig.getListOrEmpty("depend");
        this.softDependencies = pluginConfig.getListOrEmpty("softdepend");
        this.version = new Version(pluginConfig.getOrDefault("version","1.0.0"));
        this.name = pluginConfig.getOrDefault("name","");
    }

    PluginDescription() {
        this.main = FocessQQ.MainPlugin.class.getName();
        this.author = "MidCoard";
        this.dependencies = Lists.newArrayList();
        this.softDependencies = Lists.newArrayList();
        this.version = FocessQQ.getVersion();
        this.name = "MainPlugin";
    }

    public String getMain() {
        return this.main;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public List<String> getSoftDependencies() {
        return softDependencies;
    }


    public String getName() {
        return this.name;
    }

    public String getAuthor() {
        return this.author;
    }

    public Version getVersion() {
        return this.version;
    }
}
