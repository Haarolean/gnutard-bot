package dev.haarolean.gnutardbot.abilities;

import dev.haarolean.gnutardbot.TardBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Slf4j
@Scope(SCOPE_PROTOTYPE)
public class BanAbility implements AbilityProvider {

    private final TardBot bot;

    public BanAbility(TardBot bot) {
        this.bot = bot;
    }

    @Override
    public Ability buildAbility() {
        return Ability
                .builder()
                .name("ban")
                .info("ban")
                .locality(Locality.GROUP)
                .privacy(Privacy.ADMIN)
                .action(this::ban)
                .post(bot::deleteMessage)
                .build();
    }

    private void ban(MessageContext ctx) {
        var message = ctx.update().getMessage();
        var reply = message.getReplyToMessage();
        if (reply == null) return;
        var bannee = reply.getFrom();

        if (bot.isAdmin(bannee.getId())) return;
        if (bot.isGroupAdmin(ctx.update(), bannee.getId())) return;

        var chatId = message.getChatId().toString();

        try {
            var banRequest = BanChatMember
                    .builder()
                    .chatId(chatId)
                    .userId(bannee.getId())
                    .revokeMessages(false)
                    .build();
            bot.execute(banRequest);

            var deleteRequest = DeleteMessage
                    .builder()
                    .chatId(chatId)
                    .messageId(message.getMessageId())
                    .build();

            bot.silent().execute(deleteRequest);
        } catch (TelegramApiException e) {
            log.error("Error", e);
        }
    }

}
