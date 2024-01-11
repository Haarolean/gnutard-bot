package dev.haarolean.gnutardbot.abilities;

import dev.haarolean.gnutardbot.TardBot;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Objects;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ReportMessageAbility implements AbilityProvider {

    private final TardBot bot;

    public ReportMessageAbility(TardBot bot) {
        this.bot = bot;
    }

    @Override
    public Ability buildAbility() {
        return Ability
                .builder()
                .name("report")
                .locality(Locality.GROUP)
                .privacy(Privacy.PUBLIC)
                .action(this::reportMessage)
                .post(bot::deleteMessage)
                .build();
    }

    private void reportMessage(MessageContext ctx) {
        var message = ctx.update().getMessage();
        var sender = message.getFrom();
        long senderId = sender.getId();
        var chatId = message.getChatId().toString();

        if (!message.isReply()) return;

        var reply = message.getReplyToMessage();
        long reportedUserId = reply.getFrom().getId();

        if(!Objects.equals(message.getChatId(), reply.getChatId())) return;
        if (bot.isAdmin(reportedUserId)) return;
        if (bot.isGroupAdmin(ctx.update(), reportedUserId)) return;

        bot.admins().forEach(adminId -> {
            try {
                bot.execute(new ForwardMessage(adminId.toString(), chatId, message.getMessageId()));
                bot.execute(new ForwardMessage(adminId.toString(), chatId, reply.getMessageId()));
            } catch (TelegramApiException e) {
                if(e.getMessage().contains("[400]") || e.getMessage().contains("[403]")) return;
                throw new RuntimeException(e);
            }
        });
    }


}
