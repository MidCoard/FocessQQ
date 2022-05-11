package top.focess.qq.core.bot;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.api.bot.BotManager;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.bot.mirai.MiraiBotManager;
import top.focess.qq.core.permission.Permission;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class BotManagerFactory {

    private static final Map<String, Supplier<BotManager>> BOT_MANAGER_MAP = Maps.newHashMap();

    private static final List<BotManager> BOT_MANAGER_LIST = Lists.newArrayList();


    static {
        register("mirai", MiraiBotManager::new);
    }

    public static void register(final String name, final Supplier<BotManager> supplier) {
        BOT_MANAGER_MAP.put(name, supplier);
    }

    @Nullable
    public static BotManager get() {
        Permission.checkPermission(Permission.GET_BOT_MANAGER);
        return get("mirai");
    }

    @Nullable
    public static BotManager get(final String key) {
        Permission.checkPermission(Permission.GET_BOT_MANAGER);
        final BotManager botManager = BOT_MANAGER_MAP.get(key).get();
        if (botManager != null)
            BOT_MANAGER_LIST.add(botManager);
        return botManager;
    }

    public static void removeAll() {
        Permission.checkPermission(Permission.REMOVE_BOT_MANAGER);
        BOT_MANAGER_LIST.forEach(BotManager::removeAll);
    }

    public static void remove(final Plugin plugin) {
        Permission.checkPermission(Permission.REMOVE_BOT_MANAGER);
        BOT_MANAGER_LIST.forEach(botManager -> botManager.remove(plugin));
    }
}
