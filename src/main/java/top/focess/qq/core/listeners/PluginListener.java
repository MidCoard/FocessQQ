package top.focess.qq.core.listeners;

import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.event.EventHandler;
import top.focess.qq.api.event.Listener;
import top.focess.qq.api.event.plugin.PluginLoadEvent;
import top.focess.qq.api.event.plugin.PluginUnloadEvent;

public class PluginListener implements Listener {

    @EventHandler
    public void onPluginLoad(@NotNull final PluginLoadEvent event) {
        FocessQQ.getLogger().info("Plugin " + event.getPlugin().getName() + " is loaded.");
        FocessQQ.getLogger().info("Author: " + event.getPlugin().getAuthor() + ", Version: " + event.getPlugin().getVersion().toString() + " .");
    }

    @EventHandler
    public void onPluginUnload(@NotNull final PluginUnloadEvent event) {
        FocessQQ.getLogger().info("Plugin " + event.getPlugin().getName() + " is unloaded.");
        FocessQQ.getLogger().info("Author: " + event.getPlugin().getAuthor() + ", Version: " + event.getPlugin().getVersion().toString() + " .");
    }
}
