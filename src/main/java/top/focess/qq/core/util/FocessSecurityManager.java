package top.focess.qq.core.util;

import top.focess.qq.FocessQQ;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.plugin.PluginCoreClassLoader;

import java.io.File;
import java.io.FilePermission;
import java.security.Permission;

public class FocessSecurityManager extends SecurityManager {
    @Override
    public void checkPermission(Permission perm, Object context) {
        this.checkPermission0(perm);
    }

    @Override
    public void checkPermission(Permission perm) {
        this.checkPermission0(perm);
    }

    private void checkPermission0(Permission perm) {
        if (perm instanceof FilePermission) {
            String path = new File(perm.getName()).getAbsolutePath();
            if (path.startsWith(FocessQQ.getMainPlugin().getDefaultFolder().getAbsolutePath()))
                for (Class<?> clazz : MethodCaller.getAllCallerClass()) {
                    Plugin plugin = PluginCoreClassLoader.getPluginByClass(clazz);
                    if (plugin != null)
                        if (!path.startsWith(plugin.getDefaultFolder().getAbsolutePath()))
                            top.focess.qq.core.permission.Permission.checkPermission(plugin, top.focess.qq.core.permission.Permission.ACCESS_MAIN_FILE);
                }
        }
    }
}
