package top.focess.qq.api.event.bot;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.event.ListenerHandler;

/**
 * Called when bot logout
 */
public class BotLogoutEvent extends BotEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * Constructs a BotLogoutEvent
     *
     * @param bot the bot
     */
    public BotLogoutEvent(Bot bot) {
        super(bot);
    }
}
