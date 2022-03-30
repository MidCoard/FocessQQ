package top.focess.qq.api.event.bot;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.event.ListenerHandler;

/**
 * Called when bot relogin
 */
public class BotReloginEvent extends BotEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * Constructs a BotReloginEvent
     *
     * @param bot the bot
     */
    public BotReloginEvent(final Bot bot) {
        super(bot);
    }
}
