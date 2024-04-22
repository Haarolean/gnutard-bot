package dev.haarolean.gnutardbot.abilities

import dev.haarolean.gnutardbot.TardBot
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class UnpinDiscussionAbility(private val bot: TardBot) : AbilityHandler {
    override fun isApplicable(ctx: MessageContext): Boolean {
        if (!ctx.update().hasMessage()) return false
        val message = ctx.update().message
        return message.isAutomaticForward == true
    }

    override fun handle(ctx: MessageContext) {
        val message = ctx.update().message
        bot.silent().execute(UnpinChatMessage(message.chatId.toString(), message.messageId))
    }
}
