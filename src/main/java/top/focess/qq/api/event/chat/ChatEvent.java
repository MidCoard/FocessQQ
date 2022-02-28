package top.focess.qq.api.event.chat;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.event.ListenerHandler;
import top.focess.qq.api.event.bot.BotEvent;
import net.mamoe.mirai.message.data.MessageChain;

/**
 * Called when a friend chat with bot or a member chat in a group the bot is in
 */
public class ChatEvent extends BotEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The chat message
     */
    private final MessageChain message;

    /**
     * Constructs a ChatEvent
     *
     * @param bot the bot
     * @param message the chat message
     */
    public ChatEvent(Bot bot, MessageChain message) {
        super(bot);
        this.message = message;
    }

    public MessageChain getMessage() {
        return message;
    }
}
