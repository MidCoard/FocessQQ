package top.focess.qq.core.util;

import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.permission.Permission;
import top.focess.qq.core.plugin.PluginCoreClassLoader;

public class FocessSecurityManager extends SecurityManager {

    @Override
    public void checkRead(String file) {
        for (Class<?> clazz : MethodCaller.getAllCallerClass()) {
            Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(clazz);
            if (plugin != null)
                if (!file.startsWith(plugin.getDefaultFolder().getAbsolutePath()))
                    Permission.checkPermission(plugin,Permission.ACCESS_OTHER_FILE);
        }
        super.checkRead(file);
    }

}
