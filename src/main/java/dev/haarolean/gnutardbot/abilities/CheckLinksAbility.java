package dev.haarolean.gnutardbot.abilities;

import dev.haarolean.gnutardbot.TardBot;
import dev.haarolean.gnutardbot.util.MemberUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.objects.*;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.telegram.abilitybots.api.objects.Flag.EDITED_MESSAGE;
import static org.telegram.abilitybots.api.util.AbilityUtils.isUserMessage;

@Component
@Scope(SCOPE_PROTOTYPE)
public class CheckLinksAbility implements AbilityProvider {

    private final MemberUtils memberUtils;
    private final TardBot bot;

    public CheckLinksAbility(TardBot bot) {
        this.bot = bot;
        this.memberUtils = new MemberUtils(bot);
    }

    @Override
    public Ability buildAbility() {
        return Ability
                .builder()
                .name("default")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    if (!shouldProceed(ctx)) return;
                    checkLinks(ctx);
                })
                .build();
    }

    private boolean shouldProceed(MessageContext ctx) {
        // a shitty fix for a more shitty "bug":
        // https://github.com/rubenlagus/TelegramBots/issues/1296
        var update = ctx.update();
        if (!isUserMessage(update)) return true;
        if (Flag.MESSAGE.test(update) || EDITED_MESSAGE.test(update)) return false;
        return false;
    }

    private void checkLinks(MessageContext ctx) {
        var message = ctx.update().getMessage();
        var sender = message.getFrom();
        long senderId = sender.getId();
        var chatId = message.getChatId().toString();

        if (bot.isAdmin(senderId)) return;
        if (bot.isGroupAdmin(ctx.update(), senderId)) return;
        if (memberUtils.isUserKnown(chatId, senderId)) return;

        var hasLink = message.getText().matches(".*https?://.*");

        if (!hasLink) return;

        var messageText = "Yo nibba, you triggered an anti spam thingy. " +
                "No links are allowed if you're not in the chat group. " +
                "Contact the admins if you're not a bot.";
        var senderTag = !sender.getUserName().isEmpty() ? ("@" + sender.getUserName() + " ") : "";
        var replyMessage = bot.silent().forceReply(senderTag + messageText, message.getChatId());

        var banRequest = BanChatMember
                .builder()
                .chatId(chatId)
                .userId(senderId)
                .build();

        bot.silent().execute(banRequest);
        replyMessage.ifPresent(msg ->
                bot.silent().execute(new DeleteMessage(chatId, msg.getMessageId())));
    }
}
