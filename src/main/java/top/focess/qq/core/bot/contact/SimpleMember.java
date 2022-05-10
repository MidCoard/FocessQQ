package top.focess.qq.core.bot.contact;

import org.jetbrains.annotations.NotNull;
import top.focess.command.CommandPermission;
import top.focess.qq.api.bot.contact.Group;
import top.focess.qq.api.bot.contact.Member;

public class SimpleMember extends SimpleContact implements Member {

    private final Group simpleGroup;
    private final String name;
    private final String rawName;
    private final String cardName;
    private final CommandPermission commandPermission;

    public SimpleMember(@NotNull final Group group, final long id, final String name, final String rawName, final String cardName, final CommandPermission permission) {
        super(group.getBot(), id);
        this.simpleGroup = group;
        this.name = name;
        this.rawName = rawName;
        this.cardName = cardName;
        this.commandPermission = permission;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getRawName() {
        return this.rawName;
    }

    @Override
    public String getCardName() {
        return this.cardName;
    }

    @Override
    public Group getGroup() {
        return this.simpleGroup;
    }

    @Override
    public CommandPermission getPermission() {
        return this.commandPermission;
    }
}
