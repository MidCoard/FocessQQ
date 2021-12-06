package com.focess.core.listener;

import com.focess.Main;
import com.focess.api.annotation.EventHandler;
import com.focess.api.event.Listener;
import com.focess.api.event.plugin.PluginLoadEvent;
import com.focess.api.event.plugin.PluginUnloadEvent;

public class PluginListener implements Listener {

    @EventHandler
    public void onPluginLoad(PluginLoadEvent event) {
        Main.getLogger().info("Plugin: " + event.getPlugin().getName() + " is loaded. Author: " + event.getPlugin().getAuthor() + " .Version: " + event.getPlugin().getVersion().toString() + " .");
    }

    @EventHandler
    public void onPluginUnload(PluginUnloadEvent event) {
        Main.getLogger().info("Plugin: " + event.getPlugin().getName() + " is unloaded. Author: " + event.getPlugin().getAuthor() + " .Version: " + event.getPlugin().getVersion().toString() + " .");
    }
}