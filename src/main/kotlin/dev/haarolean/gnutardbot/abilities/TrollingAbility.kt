package dev.haarolean.gnutardbot.abilities

import dev.haarolean.gnutardbot.TardBot
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import java.util.regex.Pattern

private val logger = KotlinLogging.logger {}

private val replyMessages = arrayOf(
    "Windows? I'd rather deal with a real window... at least it's transparent!",
    "Windows? It's like giving someone a bicycle, but first removing the wheels",
    "Windows? I didn't realize we were in the business of collecting viruses",
    "Oh, windows. Where you have to turn it off and on again, more than any light switch",
    "Did someone say windows? I'd rather jump out of one than use it!",
    "Oh, 'windows'. The only thing that crashes more than my code."
)

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class TrollingAbility(private val bot: TardBot) : AbilityHandler {
    private val windowsPattern = Pattern.compile(".*windows.*", Pattern.CASE_INSENSITIVE)

    override fun isApplicable(ctx: MessageContext): Boolean {
        val update = ctx.update()
        val message: Message = when {
            update.hasMessage() -> update.message
            update.hasEditedMessage() -> update.editedMessage
            else -> {
                logger.debug { "TrollingAbility: update ${update.updateId} has no message or editedMessage, skipping" }
                return false
            }
        }
        if (message.isUserMessage) {
            logger.debug { "TrollingAbility: msgId=${message.messageId} is a user message, skipping" }
            return false
        }
        if (message.isChannelMessage) {
            logger.debug { "TrollingAbility: msgId=${message.messageId} is a channel message, skipping" }
            return false
        }
        if (!message.hasText()) {
            logger.debug { "TrollingAbility: msgId=${message.messageId} has no text, skipping" }
            return false
        }
        val matches = windowsPattern.matcher(message.text).matches()
        if (matches) {
            logger.info { "TrollingAbility: msgId=${message.messageId} matched 'windows' pattern, text='${message.text}'" }
        }
        return matches
    }

    override fun handle(ctx: MessageContext) {
        trolling(ctx)
    }

    private fun trolling(ctx: MessageContext) {
        var message = ctx.update().message
        if (!ctx.update().hasMessage()) message = ctx.update().editedMessage
        val chatId = message.chatId
        val messageId = message.messageId
        val messageText = replyMessages.random()
        logger.info { "TrollingAbility: sending reply to msgId=$messageId in chat=$chatId, reply='$messageText'" }
        val replyRequest = SendMessage
            .builder()
            .replyToMessageId(messageId)
            .chatId(chatId)
            .text(messageText)
            .build()
        val result = bot.silent().execute(replyRequest)
        if (result.isPresent) {
            logger.info { "TrollingAbility: reply sent successfully, replyMsgId=${result.get().messageId}" }
        } else {
            logger.warn { "TrollingAbility: failed to send reply to msgId=$messageId in chat=$chatId" }
        }
    }
}
