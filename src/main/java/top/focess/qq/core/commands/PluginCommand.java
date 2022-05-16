package top.focess.qq.core.commands;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import top.focess.command.CommandArgument;
import top.focess.command.CommandResult;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.command.converter.PermissionDataConverter;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.permission.Permission;

import java.util.List;

public class PluginCommand extends Command {

    public PluginCommand() {
        super("plugin");
    }

    @Override
    public void init() {
        this.setExecutorPermission(i -> i.isAdministrator() || i.isConsole());
        this.addExecutor((sender, data, ioHandler) -> {
            if (FocessQQ.getPlugins().size() != 0) {
                final StringBuilder stringBuilder = new StringBuilder(FocessQQ.getLangConfig().get("plugin-command-list"));
                for (final Plugin plugin : FocessQQ.getPlugins())
                    stringBuilder.append(' ').append(plugin.getName());
                stringBuilder.append('(').append(FocessQQ.getPlugins().size()).append(')');
                ioHandler.output(stringBuilder.toString());
            } else ioHandler.outputLang("plugin-command-no-plugin");
            return CommandResult.ALLOW;
        }, CommandArgument.of("list"));
        this.addExecutor((sender, data, ioHandler) -> {
            Permission permission = data.get(Permission.class);
            StringBuilder stringBuilder = new StringBuilder();
            for (final Plugin plugin : FocessQQ.getPlugins())
                if (plugin.getPluginDescription().hasPermission(permission))
                    stringBuilder.append(' ').append(plugin.getName());
            if (stringBuilder.length() != 0) {
                stringBuilder.insert(0, String.format(FocessQQ.getLangConfig().get("plugin-command-list-permission"),permission.getName()));
                ioHandler.output(stringBuilder.toString());
            } else ioHandler.outputLang("plugin-command-no-permission-matched", permission.getName());
            return CommandResult.ALLOW;
        },CommandArgument.of("permission"), CommandArgument.of(PermissionDataConverter.PERMISSION_DATA_CONVERTER) );
    }

    @Override
    @NotNull
    public List<String> usage(final CommandSender sender) {
        return Lists.newArrayList("Use: plugin list");
    }

}
