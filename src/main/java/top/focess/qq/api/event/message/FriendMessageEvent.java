package top.focess.qq.api.event.message;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Friend;
import top.focess.qq.api.bot.message.Message;
import top.focess.qq.api.bot.message.MessageSource;
import top.focess.qq.api.event.ListenerHandler;

/**
 * Called when a friend chat with bot (this does not execute any commands)
 */
public class FriendMessageEvent extends MessageEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The friend who chats with bot
     */
    private final Friend friend;

    /**
     * Constructs a FriendMessageEvent
     *
     * @param bot     the bot
     * @param message the chat message
     * @param friend  the friend who chats with bot
     * @param source  the source of the message
     */
    public FriendMessageEvent(final Bot bot, final Friend friend, final Message message, final MessageSource source) {
        super(bot, message, source);
        this.friend = friend;
    }

    public Friend getFriend() {
        return this.friend;
    }
}
