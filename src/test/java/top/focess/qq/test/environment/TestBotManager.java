package top.focess.qq.test.environment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.BotLoginException;
import top.focess.qq.api.bot.BotManager;
import top.focess.qq.api.bot.BotProtocol;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.scheduler.Schedulers;
import top.focess.qq.core.bot.BotManagerFactory;
import top.focess.scheduler.Scheduler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class TestBotManager implements BotManager {

    static {
        BotManagerFactory.register("test", TestBotManager::new);
    }

    protected static final Scheduler SCHEDULER = Schedulers.newFocessScheduler(FocessQQ.getMainPlugin(), "BotManager");
    protected static final Map<Plugin, List<Bot>> PLUGIN_BOT_MAP = Maps.newHashMap();
    protected static final Map<Long, Bot> BOTS = Maps.newConcurrentMap();

    @Override
    public Future<Bot> login(long id, String password, Plugin plugin, BotProtocol botProtocol) {
        return SCHEDULER.submit(() -> loginDirectly(id, password, plugin, botProtocol), "login-bot-" + id);
    }

    @Override
    public @NotNull Bot loginDirectly(long id, String password, Plugin plugin, BotProtocol botProtocol) throws BotLoginException {
        Bot bot = new TestBot(id, password, plugin, botProtocol,this);
        bot.login();
        BOTS.put(id, bot);
        PLUGIN_BOT_MAP.compute(plugin, (k, v) -> {
            if (v == null)
                v = Lists.newArrayList();
            v.add(bot);
            return v;
        });
        return bot;
    }

    @Override
    public boolean logout(@NotNull Bot bot) {
        return bot.logout();
    }

    @Override
    public boolean login(@NotNull Bot b) throws BotLoginException {
        return b.login();
    }

    @Override
    public @Nullable Bot getBot(long username) {
        return BOTS.get(username);
    }

    @Override
    public boolean relogin(@NotNull Bot bot) throws BotLoginException {
        return bot.relogin();
    }

    @Override
    public @UnmodifiableView List<Bot> getBots() {
        return Collections.unmodifiableList(Lists.newArrayList(BOTS.values()));
    }

    @Override
    public @Nullable Bot remove(long id) {
        Bot bot = BOTS.remove(id);
        if (bot != null)
            bot.logout();
        return bot;
    }

    @Override
    public void removeAll() {
        for (final Long id : BOTS.keySet())
            this.remove(id);
        //remove default bot
        final Bot b = BOTS.remove(FocessQQ.getBot().getId());
        if (b != null)
            b.logout();
    }

    public void remove(final Plugin plugin) {
        for (final Bot b : PLUGIN_BOT_MAP.getOrDefault(plugin, Lists.newArrayList()))
            FocessQQ.getBotManager().remove(b.getId());
        PLUGIN_BOT_MAP.remove(plugin);
    }

}
