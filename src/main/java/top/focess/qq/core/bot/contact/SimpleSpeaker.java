package top.focess.qq.core.bot.contact;

import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Speaker;
import top.focess.qq.api.bot.message.Audio;
import top.focess.qq.core.permission.Permission;
import top.focess.qq.core.permission.PermissionEnv;

import java.io.InputStream;

@PermissionEnv(values = Permission.UPLOAD_AUDIO)
public abstract class SimpleSpeaker extends SimpleTransmitter implements Speaker {

    public SimpleSpeaker(final Bot bot, final long id) {
        super(bot, id);
    }

    @Override
    public @Nullable Audio uploadAudio(final InputStream inputStream) {
        Permission.checkPermission(Permission.UPLOAD_AUDIO);
        return this.getBot().uploadAudio(this, inputStream);
    }
}
