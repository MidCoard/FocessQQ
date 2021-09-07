package com.focess.api.event;

public interface Cancelable {

    boolean isCancelled();

    void setCancelled(boolean isCancelled);
}
