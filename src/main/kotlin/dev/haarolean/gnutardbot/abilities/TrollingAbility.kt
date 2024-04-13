package dev.haarolean.gnutardbot.abilities

import dev.haarolean.gnutardbot.TardBot
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import java.util.regex.Pattern
import kotlin.random.Random

private val replyMessages = arrayOf(
    "Windows? I'd rather deal with a real window... at least it's transparent!",
    "Windows? It's like giving someone a bicycle, but first removing the wheels",
    "Windows? I didn't realize we were in the business of collecting viruses",
    "Oh, windows. Where you have to turn it off and on again, more than any light switch",
    "Did someone say windows? I'd rather jump out of one than use it!",
    "Oh, 'windows'. The only thing that crashes more than my code."
)
class TrollingAbility(private val bot: TardBot) : AbilityHandler {
    private val windowsPattern = Pattern.compile(".*windows.*")

    override fun isApplicable(ctx: MessageContext): Boolean {
        val update = ctx.update()
        val message: Message = when {
            update.hasMessage() -> update.message
            update.hasEditedMessage() -> update.editedMessage
            else -> return false
        }
        if(update.message.isUserMessage) return false
        if(update.message.isChannelMessage) return false
        if(!message.hasText()) return false
        return windowsPattern.matcher(message.text).matches()
    }

    override fun handle(ctx: MessageContext) {
        trolling(ctx)
    }

    private fun trolling(ctx: MessageContext) {
        var message = ctx.update().message
        if (!ctx.update().hasMessage()) message = ctx.update().editedMessage
        val chatId = message.chatId
        val messageId = message.messageId
        val messageText = replyMessages[Random.nextInt(replyMessages.size - 1)]
        val replyRequest = SendMessage
            .builder()
            .replyToMessageId(messageId)
            .chatId(chatId)
            .text(messageText)
            .build()
        bot.silent().execute(replyRequest)
    }
}
