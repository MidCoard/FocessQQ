package com.focess.api;

import com.focess.commands.LoadCommand;
import com.focess.util.yaml.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;

public abstract class Plugin {

    private YamlConfiguration configuration;

    private File config;

    private static final String path = Plugin.class.getProtectionDomain().getCodeSource().getLocation().getFile();

    public String getName() {
        return name;
    }

    private final String name;

    public Plugin(String name) {
        this.name = name;
        if (!getDefaultFolder().exists())
            getDefaultFolder().mkdirs();
        config = new File(getDefaultFolder(),"config.yml");
        if (!config.exists()) {
            try {
                config.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        configuration = YamlConfiguration.loadFile(getConfigFile());
    }

    public abstract void enable();

    public abstract void disable();

    public static Plugin getPlugin(Class<? extends Plugin> plugin) {
        return LoadCommand.getPlugin(plugin);
    }

    public static Plugin getPlugin(String name){return LoadCommand.getPlugin(name);}

    public File getDefaultFolder() {
        return new File(new File(new File(path).getParent(),"plugins"),this.getName());
    }

    public File getConfigFile() {
        return config;
    }

    public YamlConfiguration getConfig() {
        return configuration;
    }
}
