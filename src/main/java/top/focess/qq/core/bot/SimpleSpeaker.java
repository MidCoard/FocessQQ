package top.focess.qq.core.bot;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Message;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.Speaker;

public abstract class SimpleSpeaker extends SimpleContact implements Speaker {


    public SimpleSpeaker(Bot bot, Contact contact) {
        super(bot, contact);
    }

    @Override
    public void sendMessage(String message) {
        //todo message system
        this.contact.sendMessage(message);
    }

    @Override
    public void sendMessage(Message message) {
        this.contact.sendMessage(message);
    }
}
