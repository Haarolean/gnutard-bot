package dev.haarolean.gnutardbot.abilities

import dev.haarolean.gnutardbot.TardBot
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.GetMe
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class LoveAbility(private val bot: TardBot) : AbilityHandler {

    override fun isApplicable(ctx: MessageContext): Boolean {
        val update = ctx.update()
        if (AbilityUtils.isUserMessage(update)) return false
        val message: Message = when {
            update.hasMessage() -> update.message
            update.hasEditedMessage() -> update.editedMessage
            else -> return false
        }
        val replyMessage = message.replyToMessage
        if (replyMessage == null) return false

        val botId: Long? = bot.execute(GetMe()).id

        if (replyMessage.from.id != botId) return false

        return message.hasText() && message.text.lowercase().contains("love you")
    }

    override fun handle(ctx: MessageContext) {
        var message = ctx.update().message
        if (!ctx.update().hasMessage()) message = ctx.update().editedMessage
        val chatId = message.chatId.toString()

        val replyRequest = SendMessage
            .builder()
            .replyToMessageId(message.messageId)
            .chatId(chatId)
            .text("Love you too <3")
            .parseMode("MarkdownV2")
            .build()

        bot.silent().execute(replyRequest)
    }

}
