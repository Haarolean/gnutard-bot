package dev.haarolean.gnutardbot.abilities

import dev.haarolean.gnutardbot.TardBot
import dev.haarolean.gnutardbot.util.MemberUtils
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Message
import java.util.regex.Pattern

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class CheckLinksAbility(private val bot: TardBot) : AbilityHandler {
    private val linkPattern = Pattern.compile(".*https?://.*")
    private val memberUtils: MemberUtils = (MemberUtils(bot))

    override fun isApplicable(ctx: MessageContext): Boolean {
        val update = ctx.update()
        if (AbilityUtils.isUserMessage(update)) return false
        val message: Message = when {
            update.hasMessage() -> update.message
            update.hasEditedMessage() -> update.editedMessage
            else -> return false
        }
        val sender = message.from
        val senderId = sender.id
        val chatId = message.chatId.toString()
        if (bot.isAdmin(senderId)) return false
        if (bot.isGroupAdmin(ctx.update(), senderId)) return false
        if (memberUtils.isUserKnown(chatId, senderId)) return false
        return linkPattern.matcher(message.text).matches()
    }

    override fun handle(ctx: MessageContext) {
        checkLinks(ctx)
    }

    private fun checkLinks(ctx: MessageContext) {
        var message = ctx.update().message
        if (!ctx.update().hasMessage()) message = ctx.update().editedMessage
        val sender = message.from
        val senderId = sender.id
        val chatId = message.chatId.toString()
        val messageText = "Yo nibba, you triggered an anti spam thingy. " +
                "No links are allowed if you're not in the chat group. " +
                "Contact the admins if you're not a bot."
        val replyRequest = SendMessage
            .builder()
            .replyToMessageId(message.messageId)
            .chatId(chatId)
            .text(messageText)
            .build()
        val banRequest = BanChatMember
            .builder()
            .chatId(chatId)
            .userId(senderId)
            .build()
        val replyMessage = bot.silent().execute(replyRequest)
        bot.silent().execute(DeleteMessage(chatId, message.messageId))
        bot.silent().execute(banRequest)
        replyMessage.ifPresent { bot.silent().execute(DeleteMessage(chatId, it.messageId)) }
    }
}
