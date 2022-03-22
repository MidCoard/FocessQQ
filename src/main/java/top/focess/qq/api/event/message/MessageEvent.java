package top.focess.qq.api.event.message;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.message.MessageChain;
import top.focess.qq.api.event.ListenerHandler;
import top.focess.qq.api.event.bot.BotEvent;

/**
 * Called when a friend chat with bot or a member chat in a group the bot is in (this does not execute any commands)
 */
public class MessageEvent extends BotEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The chat message
     */
    private final MessageChain message;

    /**
     * Constructs a MessageEvent
     *
     * @param bot the bot
     * @param message the chat message
     */
    public MessageEvent(Bot bot, MessageChain message) {
        super(bot);
        this.message = message;
    }

    public MessageChain getMessage() {
        return message;
    }
}
