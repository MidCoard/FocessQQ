package top.focess.qq.api.bot.contact;

import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;

import java.util.Objects;

/**
 * Represents a friend.
 */
public interface Friend extends Speaker, CommandExecutor {

    /**
     * Get the friend's raw name (its nickname)
     *
     * @return the raw name
     */
    String getRawName();

    /**
     * Get the friend's avatar url
     *
     * @return the avatar url
     */
    @NotNull
    String getAvatarUrl();

    /**
     * Delete the friend
     */
    void delete();

    default boolean isAdministrator() {
        return Objects.equals(this.getId(), FocessQQ.getAdministratorId());
    }
}
