package dev.haarolean.gnutardbot.abilities

import dev.haarolean.gnutardbot.TardBot
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.meta.api.methods.ForwardMessage
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class ReportMessageAbility(private val bot: TardBot) : AbilityProvider {
    override fun buildAbility(): Ability {
        return Ability
            .builder()
            .name("report")
            .locality(Locality.GROUP)
            .privacy(Privacy.PUBLIC)
            .action { reportMessage(it) }
            .post { bot.deleteMessage(it) }
            .build()
    }

    private fun reportMessage(ctx: MessageContext) {
        val message = ctx.update().message
        val chatId = message.chatId.toString()
        if (!message.isReply) return
        val reply = message.replyToMessage
        val reportedUserId = reply.from.id
        if (message.chatId != reply.chatId) return
        if (bot.isAdmin(reportedUserId)) return
        if (bot.isGroupAdmin(ctx.update(), reportedUserId)) return
        bot.admins().forEach {
            try {
                bot.execute(ForwardMessage(it.toString(), chatId, message.messageId))
                bot.execute(ForwardMessage(it.toString(), chatId, reply.messageId))
            } catch (e: TelegramApiException) {
                if (e.message?.contains("[400]") == true
                        || e.message?.contains("[403]") == true) return
                throw RuntimeException(e)
            }
        }
    }
}
