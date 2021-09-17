package com.focess.api.event.request;

import com.focess.api.event.Event;
import com.focess.api.event.ListenerHandler;
import net.mamoe.mirai.contact.Group;

public class FriendRequestEvent extends Event {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    private final long id;
    private final String nick;
    private final Group group;
    private final String message;
    private Boolean accept;
    private boolean blacklist;

    public FriendRequestEvent(long id, String nick, Group group, String message) {
        this.id = id;
        this.nick = nick;
        this.group = group;
        this.message = message;
    }

    public long getId() {
        return id;
    }

    public String getNick() {
        return nick;
    }

    public Group getGroup() {
        return group;
    }

    public String getMessage() {
        return message;
    }

    public void accept() {
        this.accept = true;
    }

    public Boolean getAccept() {
        return accept;
    }

    public void refuse() {
        this.refuse(false);
    }

    public void refuse(boolean blacklist) {
        this.accept = false;
        this.blacklist = blacklist;
    }

    public boolean isBlackList() {
        return blacklist;
    }
}
