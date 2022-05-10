package top.focess.qq.api.bot.contact;

import top.focess.qq.api.bot.message.Image;
import top.focess.qq.api.bot.message.Message;

import java.io.InputStream;

/**
 * Represents a contact, which can send message and upload image.
 */
public interface Transmitter extends Contact {

    /**
     * Send a message to this contact
     * @param message the message to send
     */
    void sendMessage(String message);

    /**
     * Send a message to this contact
     * @param message the message to send
     */
    void sendMessage(Message message);

    /**
     * Upload a image to this contact
     * @param resource the image to upload
     * @return the uploaded image
     */
    Image uploadImage(InputStream resource);
}
