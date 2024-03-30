package dev.haarolean.gnutardbot.util

import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class MemberUtils(private val bot: AbilityBot) {

    fun isUserKnown(chatId: String, userId: Long): Boolean {
        val status = getStatus(chatId, userId)
        return if ("left".equals(status, ignoreCase = true)) false else !NONE.equals(status, ignoreCase = true)
    }

    private fun getStatus(chatId: String, userId: Long): String {
        val chatMember: ChatMember?
        try {
            chatMember = bot.execute(GetChatMember(chatId, userId))
            if (chatMember == null) return NONE

            return chatMember.status
        } catch (e: TelegramApiException) {
            if (e.message?.contains("[400] Bad Request: user not found") == true) {
                return NONE
            }
            throw RuntimeException(e)
        }
    }

    companion object {
        private const val NONE = "none"
    }
}
