package dev.haarolean.gnutardbot.abilities

import dev.haarolean.gnutardbot.TardBot
import org.telegram.abilitybots.api.objects.MessageContext
import kotlin.random.Random

class TrollingAbility(private val bot: TardBot): AbilityHandler {
    private val replyMessages = arrayOf(
        "Windows? I'd rather deal with a real window... at least it's transparent!",
        "Windows? It's like giving someone a bicycle, but first removing the wheels",
        "Windows? I didn't realize we were in the business of collecting viruses",
        "Oh, windows. Where you have to turn it off and on again, more than any light switch",
        "Did someone say windows? I'd rather jump out of one than use it!",
        "Oh, 'windows'. The only thing that crashes more than my code."
    )

    override fun isApplicable(ctx: MessageContext): Boolean {
        var message = ctx.update().message
        if (message == null) message = ctx.update().editedMessage
        if (message == null) return false
        if(message.text.isEmpty()) return false
        return message.text.contains("windows".toRegex(RegexOption.IGNORE_CASE))
    }

    override fun handle(ctx: MessageContext) {
        trolling(ctx)
    }

    private fun trolling(ctx: MessageContext) {
        var message = ctx.update().message
        if (message == null) message = ctx.update().editedMessage
        val senderTag = '@' + message.from.userName
        val chatId = message.chatId
        val messageText = senderTag + ' ' + replyMessages[Random.nextInt(replyMessages.size - 1)]
        bot.silent().forceReply(messageText, chatId)
    }
}