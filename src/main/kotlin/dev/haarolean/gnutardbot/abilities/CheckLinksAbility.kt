package dev.haarolean.gnutardbot.abilities

import dev.haarolean.gnutardbot.TardBot
import dev.haarolean.gnutardbot.util.MemberUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.objects.*
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Message

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class CheckLinksAbility(private val bot: TardBot) : AbilityProvider {

    private val memberUtils: MemberUtils = (MemberUtils(bot))

    override fun buildAbility(): Ability {
        return Ability
                .builder()
                .name("default")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action { ctx: MessageContext ->
                    if (!shouldProceed(ctx)) return@action
                    checkLinks(ctx)
                }
                .build()
    }

    private fun shouldProceed(ctx: MessageContext): Boolean {
        // a shitty fix for a more shitty "bug":
        // https://github.com/rubenlagus/TelegramBots/issues/1296
        val update = ctx.update()
        if (!AbilityUtils.isUserMessage(update)) return true
        return if (Flag.MESSAGE.test(update) || Flag.EDITED_MESSAGE.test(update)) false else false
    }

    private fun checkLinks(ctx: MessageContext) {
        var message = ctx.update().message
        if (message == null) message = ctx.update().editedMessage
        if (message == null) return
        val sender = message.from
        val senderId = sender.id
        val chatId = message.chatId.toString()
        if (bot.isAdmin(senderId)) return
        if (bot.isGroupAdmin(ctx.update(), senderId)) return
        if (memberUtils.isUserKnown(chatId, senderId)) return
        val hasLink = message.text.matches(".*https?://.*".toRegex())
        if (!hasLink) return
        val messageText = "Yo nibba, you triggered an anti spam thingy. " +
                "No links are allowed if you're not in the chat group. " +
                "Contact the admins if you're not a bot."
        val senderTag = if (StringUtils.isNotEmpty(sender.userName)) "@" + sender.userName + " " else ""
        val replyMessage = bot.silent().forceReply(senderTag + messageText, message.chatId)
        val banRequest = BanChatMember
                .builder()
                .chatId(chatId)
                .userId(senderId)
                .build()
        bot.silent().execute(DeleteMessage(chatId, message.messageId))
        bot.silent().execute(banRequest)
        replyMessage.ifPresent { msg: Message -> bot.silent().execute(DeleteMessage(chatId, msg.messageId)) }
    }
}
