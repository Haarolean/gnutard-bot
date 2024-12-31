package dev.haarolean.gnutardbot.abilities

import dev.haarolean.gnutardbot.TardBot
import dev.haarolean.gnutardbot.util.MemberUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Message
import kotlin.time.Duration.Companion.seconds

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class CheckLinksAbility(private val bot: TardBot) : AbilityHandler {
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

        if (!message.hasEntities()) return false

        return message.entities.stream().anyMatch { it.type == "url" || it.type == "text_link" }
    }

    override fun handle(ctx: MessageContext) {
        checkLinks(ctx)
    }

    private fun checkLinks(ctx: MessageContext) {
        var message = ctx.update().message
        if (!ctx.update().hasMessage()) message = ctx.update().editedMessage
        val chatId = message.chatId.toString()

        val sender = message.from
        val senderName = escapeMarkdownV2(sender.firstName + " " + (sender.lastName ?: ""))
        val mention = "[${senderName}](tg://user?id=${message.from.id})"

        val messageText = "$mention, the link has been removed" +
                " as no links are allowed if you're not a member of the chat group"

        val replyRequest = SendMessage
            .builder()
            .replyToMessageId(message.messageId)
            .chatId(chatId)
            .text(messageText)
            .parseMode("MarkdownV2")
            .build()
        val replyMessage = bot.silent().execute(replyRequest)

        bot.silent().execute(DeleteMessage(chatId, message.messageId))

        replyMessage.ifPresent {
            CoroutineScope(Dispatchers.IO).launch {
                delay(60.seconds)
                bot.silent().execute(DeleteMessage(chatId, it.messageId))
            }
        }
    }

    fun escapeMarkdownV2(text: String): String {
        val specialCharacters = "_*[]()~`>#+-=|{}.!"
        return text.map { if (it in specialCharacters) "\\$it" else it }.joinToString("")
    }

}
