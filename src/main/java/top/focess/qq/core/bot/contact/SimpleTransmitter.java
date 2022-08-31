package top.focess.qq.core.bot.contact;

import org.jetbrains.annotations.NotNull;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Transmitter;
import top.focess.qq.api.bot.message.Image;
import top.focess.qq.api.bot.message.Message;
import top.focess.qq.core.permission.Permission;
import top.focess.qq.core.permission.PermissionEnv;

import java.io.InputStream;

@PermissionEnv(values = {Permission.UPLOAD_IMAGE, Permission.SEND_MESSAGE})
public abstract class SimpleTransmitter extends SimpleContact implements Transmitter {


    public SimpleTransmitter(final Bot bot, final long id) {
        super(bot, id);
    }

    @Override
    public void sendMessage(final String message) {
        Permission.checkPermission(Permission.SEND_MESSAGE);
        this.getBot().sendMessage(this,message);
    }

    @Override
    public void sendMessage(@NotNull final Message message) {
        Permission.checkPermission(Permission.SEND_MESSAGE);
        this.getBot().sendMessage(this,message);
    }

    @Override
    public Image uploadImage(final InputStream inputStream) {
        Permission.checkPermission(Permission.UPLOAD_IMAGE);
        return this.getBot().uploadImage(this, inputStream);
    }
}
