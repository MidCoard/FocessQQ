package top.focess.qq.core.bot.contact;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Stranger;

public class SimpleStranger extends SimpleTransmitter implements Stranger {

    private final String name;
    private final String rawName;

    private SimpleStranger(final Bot bot, final long id, final String name, final String rawName) {
        super(bot, id);
        this.name = name;
        this.rawName = rawName;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getRawName() {
        return this.rawName;
    }
}
