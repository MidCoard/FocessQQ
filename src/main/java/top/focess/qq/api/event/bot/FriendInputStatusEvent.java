package top.focess.qq.api.event.bot;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Friend;
import top.focess.qq.api.event.ListenerHandler;

/**
 * Called when friend input status is changed
 */
public class FriendInputStatusEvent extends BotEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The friend
     */
    private final Friend friend;
    /**
     * Indicate the friend input status
     */
    private final boolean input;

    /**
     * Constructs a FriendInputStatusEvent
     *
     * @param bot    the bot
     * @param friend the friend
     * @param input  the friend input status
     */
    public FriendInputStatusEvent(final Bot bot, final Friend friend, final boolean input) {
        super(bot);
        this.friend = friend;
        this.input = input;
    }

    public boolean isInput() {
        return this.input;
    }

    public Friend getFriend() {
        return this.friend;
    }
}
