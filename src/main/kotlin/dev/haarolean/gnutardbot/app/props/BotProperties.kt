package dev.haarolean.gnutardbot.app.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("bot")
//@Configuration
//@Validated
data class BotProperties(
     val token:  String,
     val chatId: String,
     val admins: Set<Long>
)
