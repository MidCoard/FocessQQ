package com.focess.core.commands.util;

import com.focess.core.plugin.PluginClassLoader;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public interface ResourceHandler {

    void handle(@NotNull String name, @NotNull InputStream resource, @NotNull PluginClassLoader pluginClassLoader);
}
