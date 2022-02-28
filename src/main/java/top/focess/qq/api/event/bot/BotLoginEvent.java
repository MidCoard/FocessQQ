package top.focess.qq.api.event.bot;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.event.ListenerHandler;

/**
 * Called when bot login
 */
public class BotLoginEvent extends BotEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * Constructs a BotLoginEvent
     *
     * @param bot the bot
     */
    public BotLoginEvent(Bot bot) {
        super(bot);
    }
}
