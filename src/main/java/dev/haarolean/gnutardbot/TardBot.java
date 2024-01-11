package dev.haarolean.gnutardbot;

import dev.haarolean.gnutardbot.abilities.BanAbility;
import dev.haarolean.gnutardbot.abilities.CheckLinksAbility;
import dev.haarolean.gnutardbot.abilities.NukeAbility;
import dev.haarolean.gnutardbot.abilities.ReportMessageAbility;
import dev.haarolean.gnutardbot.app.props.BotProperties;
import dev.haarolean.gnutardbot.util.AdminDatabaseAdapter;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.toggle.CustomToggle;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
@Slf4j
public class TardBot extends AbilityBot {

    private static final CustomToggle toggle = new CustomToggle()
            .toggle("ban", "botBan")
            .toggle("unban", "botUnban")
            .toggle("report", "cmdList")
            .turnOff("commands");
    private static final String BOT_NAME = "GnuTardBot";
    private static final DB db = DBMaker.fileDB("/tmp/" + BOT_NAME)
            .fileMmapEnableIfSupported()
            .closeOnJvmShutdown()
            .transactionEnable()
            .make();

    public TardBot(BotProperties properties) {
        super(properties.getToken(), BOT_NAME, new AdminDatabaseAdapter(db, properties.getAdmins()), toggle);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            log.error("Error", e);
        }
    }

    @Override
    public long creatorId() {
        return 177742375; // @haarolean
    }

    @Override
    public void onRegister() {
        log.info("Bot registered");
        super.onRegister();
    }

    @Override
    protected String getCommandPrefix() {
        return "!";
    }

    @SuppressWarnings("unused")
    public Ability ban() {
        return new BanAbility(this).buildAbility();
    }

    @SuppressWarnings("unused")
    public Ability checkLinks() {
        return new CheckLinksAbility(this).buildAbility();
    }

    @SuppressWarnings("unused")
    public Ability report() {
        return new ReportMessageAbility(this).buildAbility();
    }

    @SuppressWarnings("unused")
    public Ability nuke() {
        return new NukeAbility(this).buildAbility();
    }

    public void deleteMessage(MessageContext ctx) {
        var message = ctx.update().getMessage();
        var chatId = message.getChatId().toString();
        var request = DeleteMessage
                .builder()
                .chatId(chatId)
                .messageId(message.getMessageId())
                .build();

        silent().execute(request);
    }

}
