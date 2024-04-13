package dev.haarolean.gnutardbot.abilities

import dev.haarolean.gnutardbot.TardBot
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Scope
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

private val logger = KotlinLogging.logger {}

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class NukeAbility(private val bot: TardBot) : AbilityProvider {

    override fun buildAbility(): Ability {
        return Ability
                .builder()
                .name("spam")
                .info("Nuke spammer")
                .locality(Locality.GROUP)
                .privacy(Privacy.ADMIN)
                .action { ctx: MessageContext -> nuke(ctx) }
                .post { ctx: MessageContext -> bot.deleteMessage(ctx) }
                .build()
    }


    private fun nuke(ctx: MessageContext) {
        val message = ctx.update().message

        val reply = message.replyToMessage ?: return
        val target = reply.from
        if (bot.isAdmin(target.id)) return
        if (bot.isGroupAdmin(ctx.update(), target.id)) return
        val chatId = message.chatId.toString()
        try {
            val request = BanChatMember
                    .builder()
                    .chatId(chatId)
                    .userId(target.id)
                    .revokeMessages(true)
                    .build()
            bot.execute(request)
        } catch (e: TelegramApiException) {
            logger.error(e) { "Error" }
        }
        bot.silent().execute(DeleteMessage(chatId, reply.messageId))
    }
}
