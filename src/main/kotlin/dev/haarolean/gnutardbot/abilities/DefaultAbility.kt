package dev.haarolean.gnutardbot.abilities

import dev.haarolean.gnutardbot.TardBot
import dev.haarolean.gnutardbot.util.MemberUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.objects.*
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.ChatPermissions
import org.telegram.telegrambots.meta.api.objects.Message
import java.time.Instant
import kotlin.random.Random

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class DefaultAbility(private val bot: TardBot) : AbilityProvider {

    private val memberUtils: MemberUtils = (MemberUtils(bot))

    override fun buildAbility(): Ability {
        return Ability
                .builder()
                .name("default")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action {
                    when {
                        shouldProceedWtf(it) -> windows(it)
                        shouldProceedCheckLinks(it) -> checkLinks(it)
                    }
                }
                .build()
    }

    private fun shouldProceedCheckLinks(ctx: MessageContext): Boolean {
        // a shitty fix for a more shitty "bug":
        // https://github.com/rubenlagus/TelegramBots/issues/1296
        val update = ctx.update()
        if (!AbilityUtils.isUserMessage(update)) return true
        if (Flag.MESSAGE.test(update) || Flag.EDITED_MESSAGE.test(update)) return false
        return true
    }

    private fun checkLinks(ctx: MessageContext) {
        var message = ctx.update().message
        if (message == null) message = ctx.update().editedMessage
        if (message == null) return
        val sender = message.from
        val senderId = sender.id
        val chatId = message.chatId.toString()
        if (bot.isAdmin(senderId)) return
        if (bot.isGroupAdmin(ctx.update(), senderId)) return
        if (memberUtils.isUserKnown(chatId, senderId)) return
        val hasLink = message.text.matches(".*https?://.*".toRegex())
        if (!hasLink) return
        val messageText = "Yo nibba, you triggered an anti spam thingy. " +
                "No links are allowed if you're not in the chat group. " +
                "Contact the admins if you're not a bot."
        val senderTag = if (StringUtils.isNotEmpty(sender.userName)) "@" + sender.userName + " " else ""
        val replyMessage = bot.silent().forceReply(senderTag + messageText, message.chatId)
        val banRequest = BanChatMember
                .builder()
                .chatId(chatId)
                .userId(senderId)
                .build()
        bot.silent().execute(DeleteMessage(chatId, message.messageId))
        bot.silent().execute(banRequest)
        replyMessage.ifPresent { msg: Message -> bot.silent().execute(DeleteMessage(chatId, msg.messageId)) }
    }

    private fun shouldProceedWtf(ctx: MessageContext): Boolean {
        val message = ctx.update().message
        return message.text.matches("windows[?!]".toRegex(RegexOption.IGNORE_CASE))
    }

    private fun windows(ctx: MessageContext) {
        fun timeString(offset: Int) : String {
            val days = offset / (24 * 60)
            val hours = (offset % (24 * 60)) / 60
            val minutes = (offset % (24 * 60)) % 60
            var timeString = ""
            if (days > 0) timeString += "$days days"
            if (hours > 0) { if(timeString.isNotEmpty()) timeString += ", $hours hours" else timeString += "$hours hours"}
            if (minutes > 0) {
                if(timeString.isNotEmpty())  timeString += " and $minutes minutes" else timeString += "$minutes minutes"
            }
            return timeString;
        }

        var message = ctx.update().message
        if (message == null) message = ctx.update().editedMessage
        if (message == null) return

        val sender = message.from
        val senderId = sender.id
        val chatId = message.chatId.toString()

        if(!AbilityUtils.isUserMessage(ctx.update())) {
            if (bot.isAdmin(senderId)) return
            if (bot.isGroupAdmin(ctx.update(), senderId)) return
        }
        val random = Random.nextInt(100)

        val offset: Int = when {
            random < 70 -> Random.nextInt(5, 120)
            random < 95 -> Random.nextInt(120, 24*60)
            else -> Random.nextInt(24*60, 180*24*60)
        }

        val muteUntil = Instant.now().epochSecond + offset*60
        val messageText: String = when {
            offset < 30 -> {
                "You can do it better. See you in $offset minutes!"
            }

            offset < 60 -> {
                "You've earned a $offset-minute coffee break on us!"
            }

            offset < 24*60 -> {
                "You've been awarded a ${Math.round(offset / 60.0)}-hour digital detox session."
            }
            else -> {
                "Go touch some grass."
            }
        }
        val descriptionMessage = "Mute for ${timeString(offset)}"
        val senderTag = if (StringUtils.isNotEmpty(sender.userName)) "@" + sender.userName + " " else ""
        bot.silent().forceReply(senderTag + messageText + '\n' + descriptionMessage, message.chatId)
        if(!AbilityUtils.isUserMessage(ctx.update())) {
            val restrictionRequest = RestrictChatMember
                .builder()
                .chatId(chatId)
                .userId(senderId)
                .permissions(
                    ChatPermissions(
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false
                )
                )
                .untilDate(muteUntil.toInt())
                .build()
            bot.silent().execute(restrictionRequest)
        }
    }
}
