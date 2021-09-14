package com.focess.listener;

import com.focess.Main;
import com.focess.api.annotation.EventHandler;
import com.focess.api.event.ConsoleInputEvent;
import com.focess.api.event.EventPriority;
import com.focess.api.event.Listener;
import com.focess.api.util.IOHandler;
import com.focess.util.Pair;
import com.google.common.collect.Lists;

import java.util.List;

public class ConsoleListener implements Listener {

    public static final List<Pair<IOHandler,Long>> QUESTS = Lists.newLinkedList();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConsoleInput(ConsoleInputEvent event) {
        if (QUESTS.size() != 0) {
            QUESTS.remove(0).getKey().input(event.getInput());
            return;
        }
        try {
            Main.CommandLine.exec(event.getInput());
        } catch (Exception e) {
            Main.getLogger().thr("Exec Command Exception", e);
        }
    }

    public static void registerInputListener(IOHandler ioHandler) {
        QUESTS.add(Pair.of(ioHandler,System.currentTimeMillis()));
    }


}
