package top.focess.qq.core.bot;

import top.focess.qq.FocessQQ;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.BotLoginException;
import top.focess.qq.api.bot.BotManager;
import top.focess.qq.api.bot.BotProtocol;
import top.focess.qq.api.plugin.Plugin;

import java.util.Objects;

public abstract class QQBot implements Bot {

    private final long username;
    protected final String password;
    private final Plugin plugin;
    private final BotManager botManager;
    private final BotProtocol botProtocol;

    public QQBot(final long username, final String password, final Plugin plugin, final BotProtocol botProtocol, final BotManager botManager) {
        this.username = username;
        this.password = password;
        this.plugin = plugin;
        this.botManager = botManager;
        this.botProtocol = botProtocol;
    }

    @Override
    public boolean relogin() throws BotLoginException {
        return this.botManager.relogin(this);
    }

    @Override
    public boolean login() throws BotLoginException {
        return this.botManager.login(this);
    }

    @Override
    public boolean logout() {
        return this.botManager.logout(this);
    }

    @Override
    public long getId() {
        return this.username;
    }

    @Override
    public boolean isDefaultBot() {
        return this.equals(FocessQQ.getBot());
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    public boolean isAdministrator() {
        if (FocessQQ.getAdministratorId() != null)
            return FocessQQ.getAdministratorId() == this.getId();
        return false;
    }

    @Override
    public BotManager getBotManager() {
        return this.botManager;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        final QQBot qqBot = (QQBot) o;

        if (this.username != qqBot.username) return false;
        return Objects.equals(this.botManager, qqBot.botManager);
    }

    @Override
    public int hashCode() {
        int result = (int) (this.username ^ (this.username >>> 32));
        result = 31 * result + (this.botManager != null ? this.botManager.hashCode() : 0);
        return result;
    }

    public String getPassword() {
        return this.password;
    }

    public BotProtocol getBotProtocol() {
        return this.botProtocol;
    }
}
