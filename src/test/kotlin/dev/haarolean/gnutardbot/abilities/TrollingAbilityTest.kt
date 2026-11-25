package dev.haarolean.gnutardbot.abilities

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User

class TrollingAbilityTest {

    private val group = Chat(1L, "supergroup")

    private fun message(id: Int, author: String, text: String?, replyTo: Message? = null) = Message().apply {
        messageId = id
        chat = group
        from = User(id.toLong(), author, false)
        this.text = text
        replyToMessage = replyTo
    }

    @Test
    fun `transcript walks the reply chain through history`() {
        val root = message(1, "alice", "my laptop won't boot")
        val question = message(2, "bob", "what os?", root)
        val sticker = message(3, "carol", null, question)
        val trigger = message(4, "alice", "windows update, again", message(3, "carol", null))
        val history = listOf(question, sticker).associateBy { it.chatId to it.messageId }

        assertEquals(
            "alice: my laptop won't boot\nbob: what os?\nalice: windows update, again",
            transcript(trigger, history)
        )
    }
}
