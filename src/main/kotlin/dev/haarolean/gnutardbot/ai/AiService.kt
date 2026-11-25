package dev.haarolean.gnutardbot.ai

import dev.haarolean.gnutardbot.app.props.AiProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

private val logger = KotlinLogging.logger {}

@Service
@EnableConfigurationProperties(AiProperties::class)
class AiService(private val properties: AiProperties) {

    private val rest = RestClient.builder()
        .baseUrl(properties.baseUrl.trimEnd('/'))
        .defaultHeader("Authorization", "Bearer ${properties.apiKey}")
        .build()

    fun chatCompletion(system: String, prompt: String): String? {
        val request = ChatRequest(
            model = properties.model,
            messages = listOf(
                ChatMessage(role = "system", content = system),
                ChatMessage(role = "user", content = prompt)
            )
        )

        try {
            val content = rest.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ChatResponse::class.java)
                ?.choices?.firstOrNull()?.message?.content
            if (!content.isNullOrBlank()) return content
            logger.warn { "AI provider returned empty response" }
        } catch (e: Exception) {
            logger.warn { "AI provider call failed: $e" }
        }
        return null
    }

    private data class ChatRequest(
        val model: String,
        val messages: List<ChatMessage>
    )

    private data class ChatMessage(
        val role: String,
        val content: String
    )

    private data class ChatResponse(
        val choices: List<Choice> = emptyList()
    )

    private data class Choice(
        val message: ResponseMessage? = null
    )

    private data class ResponseMessage(
        val content: String? = null
    )
}
