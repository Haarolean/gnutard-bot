package dev.haarolean.gnutardbot.abilities

import dev.haarolean.gnutardbot.TardBot
import org.apache.commons.lang3.StringUtils
import org.telegram.abilitybots.api.objects.Flag
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Message
import dev.haarolean.gnutardbot.util.MemberUtils

class CheckLinksAbility(private val bot: TardBot): AbilityHandler {
    private val memberUtils: MemberUtils = (MemberUtils(bot))

    override fun isApplicable(ctx: MessageContext): Boolean {
        val update = ctx.update()
        if (AbilityUtils.isUserMessage(update)) return false
        if (Flag.MESSAGE.test(update) || Flag.EDITED_MESSAGE.test(update)) return false
        var message = update.message
        if (message == null) message = update.editedMessage
        if (message == null) return false
        val sender = message.from
        val senderId = sender.id
        val chatId = message.chatId.toString()
        if (bot.isAdmin(senderId)) return false
        if (bot.isGroupAdmin(ctx.update(), senderId)) return false
        if (memberUtils.isUserKnown(chatId, senderId)) return false
        return message.text.matches(".*https?://.*".toRegex())

    }

    override fun handle(ctx: MessageContext) {
        checkLinks(ctx)
    }

    private fun checkLinks(ctx: MessageContext) {
        var message = ctx.update().message
        if (message == null) message = ctx.update().editedMessage
        val sender = message.from
        val senderId = sender.id
        val chatId = message.chatId.toString()
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
}