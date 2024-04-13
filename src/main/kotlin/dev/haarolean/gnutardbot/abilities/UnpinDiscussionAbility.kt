package dev.haarolean.gnutardbot.abilities

import dev.haarolean.gnutardbot.TardBot
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage

class UnpinDiscussionAbility(private val bot: TardBot): AbilityHandler {
    override fun isApplicable(ctx: MessageContext): Boolean {
        val message = ctx.update().message
        if(message == null) return false
        if(!message.isAutomaticForward) return false
        return true
    }

    override fun handle(ctx: MessageContext) {
        val message = ctx.update().message
        bot.silent().execute(UnpinChatMessage(message.chatId.toString(), message.messageId))
    }
}