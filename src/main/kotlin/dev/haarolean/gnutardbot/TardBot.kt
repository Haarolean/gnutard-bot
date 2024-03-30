package dev.haarolean.gnutardbot

import dev.haarolean.gnutardbot.abilities.BanAbility
import dev.haarolean.gnutardbot.abilities.CheckLinksAbility
import dev.haarolean.gnutardbot.abilities.NukeAbility
import dev.haarolean.gnutardbot.abilities.ReportMessageAbility
import dev.haarolean.gnutardbot.app.props.BotProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.mapdb.DBMaker.fileDB
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.db.MapDBContext
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.abilitybots.api.toggle.CustomToggle
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import kotlin.Long
import kotlin.Suppress

private val logger = KotlinLogging.logger {}

@Component
@EnableConfigurationProperties(BotProperties::class)
class TardBot(val properties: BotProperties)
    : AbilityBot(properties.token, BOT_NAME, MapDBContext(db), toggle) {

    init {
        try {
            val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
            botsApi.registerBot(this)
        } catch (e: TelegramApiException) {
            logger.error(e) { "Error" }
        }
    }

    override fun creatorId(): Long {
        return 177742375 // @haarolean
    }

    override fun onRegister() {
        logger.info { "Bot registered" }
        super.onRegister()
    }

    override fun admins(): Set<Long> {
        return properties.admins
    }

    @Suppress("unused")
    fun ban(): Ability {
        return BanAbility(this).buildAbility()
    }

    @Suppress("unused")
    fun checkLinks(): Ability {
        return CheckLinksAbility(this).buildAbility()
    }

    @Suppress("unused")
    fun report(): Ability {
        return ReportMessageAbility(this).buildAbility()
    }

    @Suppress("unused")
    fun nuke(): Ability {
        return NukeAbility(this).buildAbility()
    }

    override fun onUpdateReceived(update: Update) {
        unpinDiscussion(update) // can't have multiple default abilities, https://github.com/rubenlagus/TelegramBots/issues/1296
        super.onUpdateReceived(update)
    }

    private fun unpinDiscussion(update: Update) {
        if (!update.hasMessage()) return
        val message = update.message
        if (message.isAutomaticForward != true) return
        silent().execute(UnpinChatMessage(message.chatId.toString(), message.messageId))
    }

    fun deleteMessage(ctx: MessageContext) {
        val message = ctx.update().message
        val chatId = message.chatId.toString()
        val request = DeleteMessage
                .builder()
                .chatId(chatId)
                .messageId(message.messageId)
                .build()
        silent().execute(request)
    }

    companion object {
        private val toggle = CustomToggle()
                .toggle("ban", "botBan")
                .toggle("unban", "botUnban")
                .toggle("report", "cmdList")
                .turnOff("commands")
        private const val BOT_NAME = "GnuTardBot"
        private val db = fileDB("/tmp/$BOT_NAME")
                .fileMmapEnableIfSupported()
                .closeOnJvmShutdown()
                .transactionEnable()
                .make()
    }
}
