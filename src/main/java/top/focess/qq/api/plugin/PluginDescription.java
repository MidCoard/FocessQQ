package top.focess.qq.api.plugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import top.focess.command.InputTimeoutException;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.core.permission.Permission;
import top.focess.util.version.Version;
import top.focess.util.yaml.YamlConfiguration;
import top.focess.util.yaml.YamlLoadException;

import java.io.File;
import java.util.List;
import java.util.Map;

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
     * The plugin required Focess version
     */
    private final Version requireVersion;

    /**
     * The plugin limited Focess version
     */
    private final Version limitVersion;

    private final Map<Permission,Boolean> permissions = Maps.newHashMap();

    private static YamlConfiguration permissionsConfig;

    /**
     * Constructs a new PluginDescription from the plugin config
     *
     * @param pluginConfig the plugin config named "plugin.yml"
     */
    public PluginDescription(@NotNull final YamlConfiguration pluginConfig) {
        this.main = pluginConfig.get("main");
        this.author = pluginConfig.getOrDefault("author","");
        this.dependencies = pluginConfig.getListOrEmpty("depend");
        this.softDependencies = pluginConfig.getListOrEmpty("soft-depend");
        this.version = new Version(pluginConfig.getOrDefault("version","1.0.0"));
        this.name = pluginConfig.getOrDefault("name","");
        this.requireVersion = new Version(pluginConfig.getOrDefault("require-version",FocessQQ.getVersion().toString()));
        this.limitVersion = new Version(pluginConfig.getOrDefault("limit-version",FocessQQ.getVersion().toString()));
        YamlConfiguration permissionsStatus = permissionsConfig.getSection("name");
        List<String> yeses = permissionsStatus.getListOrEmpty("yes");
        List<String> nos = permissionsStatus.getListOrEmpty("no");
        List<String> permissions = Lists.newCopyOnWriteArrayList(pluginConfig.getListOrEmpty("permissions"));
        for (String permission : permissions) {
            Permission per = Permission.getPermission(permission);
            if (per == null) {
                permissions.remove(permission);
                continue;
            }
            if (yeses.contains(permission)) {
                this.permissions.put(per, true);
                permissions.remove(permission);
            } else if (nos.contains(permission)) {
                this.permissions.put(per, false);
                permissions.remove(permission);
            } else if (per.getPriority() <= 1) {
                this.permissions.put(per, true);
                permissions.remove(permission);
            } else if (per.getPriority() >= 4) {
                this.permissions.put(per, false);
                permissions.remove(permission);
            }
        }
        IOHandler.getConsoleIoHandler().outputLang("permission-before-request",this.name, permissions.size());
        Boolean isAll = null;
        try {
            String yes = IOHandler.getConsoleIoHandler().input();
            if (yes.equalsIgnoreCase("yes"))
                isAll = true;
            else if (yes.equalsIgnoreCase("no"))
                isAll = false;
        } catch (InputTimeoutException ignored) {
        }
        for (String permission : permissions) {
            if (isAll == null) {
                IOHandler.getConsoleIoHandler().outputLang("permission-request",this.name,permission);
                try {
                    String yes = IOHandler.getConsoleIoHandler().input();
                    if (yes.equalsIgnoreCase("yes")) {
                        yeses.add(permission);
                        this.permissions.put(Permission.getPermission(permission), true);
                    } else if (yes.equalsIgnoreCase("no")) {
                        nos.add(permission);
                        this.permissions.put(Permission.getPermission(permission), false);
                    }
                } catch (InputTimeoutException ignored) {
                }
            } else {
                if (isAll)
                    yeses.add(permission);
                else nos.add(permission);
                this.permissions.put(Permission.getPermission(permission), isAll);
            }
        }
        permissionsStatus.set("yes",yeses);
        permissionsStatus.set("no",nos);
        permissionsStatus.save(new File("plugins/Main","permissions.yml"));
    }

    PluginDescription() {
        this.main = FocessQQ.MainPlugin.class.getName();
        this.author = "MidCoard";
        this.dependencies = Lists.newArrayList();
        this.softDependencies = Lists.newArrayList();
        this.version = FocessQQ.getVersion();
        this.name = "Main";
        this.requireVersion = FocessQQ.getVersion();
        this.limitVersion = FocessQQ.getVersion();
        this.permissions.put(Permission.ALL,true);
        try {
            permissionsConfig = YamlConfiguration.loadFile(new File("plugins/Main","permissions.yml"));
        } catch (YamlLoadException e) {
            System.err.println("[FocessQQ][Console] -> Failed to load permissions.yml. Force shutdown.");
            FocessQQ.exit();
        }
    }

    public String getMain() {
        return this.main;
    }

    public List<String> getDependencies() {
        return this.dependencies;
    }

    public List<String> getSoftDependencies() {
        return this.softDependencies;
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

    public Version getRequireVersion() {
        return this.requireVersion;
    }

    public Version getLimitVersion() {
        return this.limitVersion;
    }

    public boolean hasPermission(Permission permission) {
        for (Permission p : permissions.keySet())
            if (permissions.get(p))
                if (p.hasPermission(permission))
                    return true;
        return false;
    }
}
