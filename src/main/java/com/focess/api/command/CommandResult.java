package com.focess.api.command;

public enum CommandResult {
    ALLOW(1), REFUSE(2), MESSAGE(4),ARGS(8), ALL(ALLOW, REFUSE, MESSAGE,ARGS), POSITIVE(ALLOW, MESSAGE), NONE(0);

    private final int pos;

    CommandResult(CommandResult result, CommandResult... results) {
        this(toInt(result, results));
    }

    CommandResult(int pos) {
        this.pos = pos;
    }

    private static int toInt(CommandResult result, CommandResult[] results) {
        int ret = result.getPos();
        for (CommandResult r : results)
            ret |= r.getPos();
        return ret;
    }

    public int getPos() {
        return pos;
    }
}
