package top.focess.qq.core.commands;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import top.focess.command.CommandArgument;
import top.focess.command.CommandResult;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.command.converter.PermissionDataConverter;
import top.focess.qq.api.command.converter.PluginDataConverter;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.permission.Permission;

import java.util.List;

public class PermissionCommand extends Command {

    public PermissionCommand() {
        super("permission");
    }

    @Override
    public void init() {
        this.setExecutorPermission(i -> i.isAdministrator() || i.isConsole());
        this.addExecutor((sender, data, ioHandler) -> {
            Plugin plugin = data.get(Plugin.class);
            if (plugin.getPluginDescription().getPermissions().isEmpty())
                ioHandler.outputLang("permission-command-no-permission", plugin.getName());
            else {
                StringBuilder stringBuilder = new StringBuilder();
                for (Permission permission : plugin.getPluginDescription().getPermissions().keySet())
                    stringBuilder.append(' ').append(permission);
                ioHandler.outputLang("permission-command-list", plugin.getName(), stringBuilder.toString());
            }
            return CommandResult.ALLOW;
        }, CommandArgument.of("get"), CommandArgument.of(PluginDataConverter.PLUGIN_DATA_CONVERTER));
        this.addExecutor((sender, data, ioHandler) -> {
            Plugin plugin = data.get(Plugin.class);
            Permission permission = data.get(Permission.class);
            if (plugin.getPluginDescription().addPermission(permission))
                ioHandler.outputLang("permission-command-set-success", plugin.getName(), permission.getName());
            else ioHandler.outputLang("permission-command-set-failed", plugin.getName(), permission.getName());
            return CommandResult.ALLOW;
        }, CommandArgument.of("set"), CommandArgument.of(PluginDataConverter.PLUGIN_DATA_CONVERTER), CommandArgument.of(PermissionDataConverter.PERMISSION_DATA_CONVERTER));
        this.addExecutor((sender, dataCollection, ioHandler) -> {
            Permission permission = dataCollection.get(Permission.class);
            StringBuilder stringBuilder = new StringBuilder();
            for (Plugin plugin : Plugin.getPlugins())
                if (plugin.getPluginDescription().getPermissions().getOrDefault(permission, false))
                    stringBuilder.append(' ').append(plugin.getName());
            ioHandler.outputLang("permission-command-list-plugin", permission.getName(), stringBuilder.toString());
            return CommandResult.ALLOW;
        }, CommandArgument.of("list"), CommandArgument.of(PermissionDataConverter.PERMISSION_DATA_CONVERTER));
    }

    @Override
    public @NotNull List<String> usage(CommandSender sender) {
        return Lists.newArrayList("Use: permission set <plugin> <permission>", "Use: permission get <plugin>", "Use: permission list <permission>");
    }
}
