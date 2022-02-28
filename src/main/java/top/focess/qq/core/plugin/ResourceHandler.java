package top.focess.qq.core.plugin;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public interface ResourceHandler {

    void handle(@NotNull String name, @NotNull InputStream resource, @NotNull PluginClassLoader pluginClassLoader);
}
