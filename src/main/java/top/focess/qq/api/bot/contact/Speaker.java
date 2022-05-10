package top.focess.qq.api.bot.contact;

import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.api.bot.message.Audio;

import java.io.InputStream;

/**
 * Represents a transmitter, which can upload audio.
 */
public interface Speaker extends Transmitter {

    @Nullable
    Audio uploadAudio(InputStream inputStream);

}
