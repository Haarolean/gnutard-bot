package dev.haarolean.gnutardbot.app.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("ai")
data class AiProperties(
    val baseUrl: String,
    val apiKey: String,
    val model: String
)
