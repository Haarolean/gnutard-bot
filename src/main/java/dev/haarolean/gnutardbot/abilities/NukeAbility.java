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
public class NukeAbility implements AbilityProvider {

    private final TardBot bot;

    public NukeAbility(TardBot bot) {
        this.bot = bot;
    }

    @Override
    public Ability buildAbility() {
        return Ability
                .builder()
                .name("spam")
                .info("Nuke spammer")
                .locality(Locality.GROUP)
                .privacy(Privacy.ADMIN)
                .action(this::nuke)
                .post(bot::deleteMessage)
                .build();
    }

    private void nuke(MessageContext ctx) {
        var message = ctx.update().getMessage();
        var reply = message.getReplyToMessage();
        if (reply == null) return;
        var target = reply.getFrom();

        if (bot.isAdmin(target.getId())) return;
        if (bot.isGroupAdmin(ctx.update(), target.getId())) return;

        var chatId = message.getChatId().toString();

        try {
            var request = BanChatMember
                    .builder()
                    .chatId(chatId)
                    .userId(target.getId())
                    .revokeMessages(true)
                    .build();
            bot.execute(request);

        } catch (TelegramApiException e) {
            log.error("Error", e);
        }

        bot.silent().execute(new DeleteMessage(chatId, reply.getMessageId()));
    }

}
