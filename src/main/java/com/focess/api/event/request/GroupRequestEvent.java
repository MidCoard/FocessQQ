package com.focess.api.event.request;

import com.focess.api.event.Event;
import com.focess.api.event.ListenerHandler;
import net.mamoe.mirai.contact.Friend;

public class GroupRequestEvent extends Event {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();
    private final long id;
    private final String name;
    private final Friend invitor;
    private Boolean accept;

    public GroupRequestEvent(long id, String name, Friend invitor) {
        this.id = id;
        this.name = name;
        this.invitor = invitor;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Friend getInvitor() {
        return invitor;
    }

    public void accept() {
        this.accept = true;
    }

    public void ignore() {
        this.accept = false;
    }

    public Boolean getAccept() {
        return accept;
    }
}
