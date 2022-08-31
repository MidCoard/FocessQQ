package top.focess.qq.core.permission;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.plugin.PluginCoreClassLoader;
import top.focess.qq.core.util.MethodCaller;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

public enum Permission {
    REGISTER_LISTENER("REGISTER_LISTENER",0),
    REGISTER_COMMAND("REGISTER_COMMAND",0),
    REGISTER_DATA_BUFFER("REGISTER_DATA_BUFFER",0),
    REGISTER_SPECIAL_ARGUMENT_COMPLEX_HANDLER("REGISTER_SPECIAL_ARGUMENT_COMPLEX_HANDLER",0),

    REGISTER("REGISTER",1,REGISTER_LISTENER,REGISTER_COMMAND,REGISTER_DATA_BUFFER,REGISTER_SPECIAL_ARGUMENT_COMPLEX_HANDLER),

    EXECUTE_NORMAL_COMMAND("EXECUTE_NORMAL_COMMAND",0),

    EXECUTE_CONSOLE_COMMAND("EXECUTE_CONSOLE_COMMAND",2),

    EXECUTE_ADMINISTRATOR_COMMAND("EXECUTE_ADMINISTRATOR_COMMAND",2),

    CANCEL_COMMAND_EXECUTE("CANCEL_COMMAND_EXECUTE",2),

    EXECUTE_COMMAND("EXECUTE_COMMAND",3, EXECUTE_NORMAL_COMMAND,EXECUTE_CONSOLE_COMMAND,EXECUTE_ADMINISTRATOR_COMMAND,CANCEL_COMMAND_EXECUTE),

    SEND_MESSAGE("SEND_MESSAGE", 2),
    UPLOAD_AUDIO("UPLOAD_AUDIO", 2),
    UPLOAD_IMAGE("UPLOAD_IMAGE", 2),
    MESSAGE("MESSAGE", 3, SEND_MESSAGE, UPLOAD_AUDIO, UPLOAD_IMAGE),

    BOT_LOGIN("BOT_LOGIN",2),
    BOT_LOGOUT("BOT_LOGOUT",2),
    BOT_RELOGIN("BOT_RELOGIN",2),
    QUIT_GROUP("QUIT_GROUP", 2),
    DELETE_FRIEND("DELETE_FRIEND", 2),

    FRIEND_REQUEST_ACCEPT("FRIEND_REQUEST_ACCEPT", 2),
    FRIEND_REQUEST_REFUSE("FRIEND_REQUEST_REFUSE", 2),

    GROUP_REQUEST_ACCEPT("GROUP_REQUEST_ACCEPT", 2),
    GROUP_REQUEST_IGNORE("GROUP_REQUEST_IGNORE", 2),

    BOT("BOT", 3, BOT_LOGIN, QUIT_GROUP, DELETE_FRIEND, BOT_LOGOUT, BOT_RELOGIN, FRIEND_REQUEST_ACCEPT, FRIEND_REQUEST_REFUSE, GROUP_REQUEST_ACCEPT, GROUP_REQUEST_IGNORE),

    NETWORK("NETWORK", 2),

    SCHEDULER("SCHEDULER", 2),
    EVENT_SUBMIT("EVENT_SUBMIT",2),
    DISABLE_PLUGIN("UNLOAD_PLUGIN",4),
    ENABLE_PLUGIN("ENABLE_PLUGIN",4),

    // no need for this permission
    NEW_PLUGIN("NEW_PLUGIN",4),
    LOAD_SOFT_DEPENDENCIES("LOAD_SOFT_DEPENDENCIES",4),

    GET_BOT_MANAGER("GET_BOT_MANAGER",4),
    EXIT("EXIT",4),

    REMOVE_BOT_MANAGER("REMOVE_BOT_MANAGER",4),
    REMOVE_COMMAND("REMOVE_COMMAND",4),
    REMOVE_DATA_BUFFER("REMOVE_DATA_BUFFER",4),
    REMOVE_LISTENER("REMOVE_LISTENER",4),
    REMOVE_SPECIAL_ARGUMENT_COMPLEX_HANDLER("REMOVE_SPECIAL_ARGUMENT_COMPLEX_HANDLER",4),
    REMOVE_SCHEDULER("REMOVE_SCHEDULER",4),

    REMOVE("REMOVE",5,REMOVE_BOT_MANAGER,REMOVE_COMMAND,REMOVE_DATA_BUFFER,REMOVE_LISTENER,REMOVE_SPECIAL_ARGUMENT_COMPLEX_HANDLER,REMOVE_SCHEDULER),

    ACCESS_MAIN_FILE("ACCESS_MAIN_FILE",4),

    INIT_PLUGIN("INIT_PLUGIN",4),
    ALL("ALL",5,REGISTER,BOT,MESSAGE,EVENT_SUBMIT,DISABLE_PLUGIN,ENABLE_PLUGIN,EXIT,LOAD_SOFT_DEPENDENCIES,NETWORK,SCHEDULER,GET_BOT_MANAGER,REMOVE, ACCESS_MAIN_FILE,INIT_PLUGIN);

    private final List<Permission> permissions;
    private final String name;
    private final int priority;

    Permission(String name, int priority, final @NotNull Permission permission, final Permission @NotNull ... permissions) {
        this.permissions = Lists.newArrayList(permission.permissions);
        for (Permission per : permissions)
            this.permissions.addAll(per.permissions);
        this.name = name;
        this.priority = priority;
    }

    Permission(String name, int priority) {
        this.permissions = Lists.newArrayList(this);
        this.name = name;
        this.priority = priority;
    }

    public static Permission getPermission(final @NotNull String name) {
        String key = name.trim().replace(" ", "_").toUpperCase();
        return Permission.valueOf(key);
    }

    public static void checkPermission(@NotNull Plugin plugin, Permission permission) {
        if (!plugin.getPluginDescription().hasPermission(permission)) {
            throw new PermissionException(plugin, permission);
        }
    }

    public String getName() {
        return this.name;
    }

    public int getPriority() {
        return this.priority;
    }

    public static void checkPermission(final Permission permission) {
        Class<?> permissionClass = MethodCaller.getCallerClass();
        if (permissionClass == null)
            throw new PermissionException(permission);
        Annotation annotation = Arrays.stream(permissionClass.getAnnotations()).filter(i -> i.annotationType() == PermissionEnv.class).findAny().orElse(null);
        if (annotation == null)
            throw new PermissionException(permission);
        PermissionEnv permissionEnv = (PermissionEnv) annotation;
        if (Arrays.stream(permissionEnv.values()).allMatch(i -> i != permission))
            throw new PermissionException(permission);
        for (Class<?> clazz : MethodCaller.getAllCallerClass()) {
            Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(clazz);
            if (plugin != null)
                checkPermission(plugin, permission);
        }
    }

    public boolean hasPermission(Permission permission) {
        return this.permissions.contains(permission);
    }
}
