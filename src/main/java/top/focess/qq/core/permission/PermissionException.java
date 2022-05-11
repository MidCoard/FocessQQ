package top.focess.qq.core.permission;

import top.focess.qq.api.plugin.Plugin;

public class PermissionException extends RuntimeException{

    public PermissionException(Plugin plugin, Permission permission) {
        super("Plugin " + plugin.getName() + " don't have permission: " + permission.getName());
    }
}
