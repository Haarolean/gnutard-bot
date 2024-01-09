package dev.haarolean.gnutardbot;

import dev.haarolean.gnutardbot.app.props.BotProperties;
import dev.haarolean.gnutardbot.util.AdminDatabaseAdapter;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.toggle.CustomToggle;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import static org.telegram.abilitybots.api.objects.Flag.TEXT;

@Component
@Slf4j
public class Bot extends AbilityBot {

    private static final CustomToggle toggle = new CustomToggle()
            .toggle("ban", "botBan")
            .toggle("unban", "botUnban");
    private static final String BOT_NAME = "GnuTardBot";
    private static final DB db = DBMaker.fileDB(BOT_NAME)
            .fileMmapEnableIfSupported()
            .closeOnJvmShutdown()
            .transactionEnable()
            .make();
    private static final String NONE = "none";

    public Bot(BotProperties properties) {
        super(properties.getToken(), BOT_NAME, new AdminDatabaseAdapter(db, properties.getAdmins()), toggle);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            log.error("Error", e);
        }
    }

    @SuppressWarnings("unused")
    public Ability ban() {
        return Ability
                .builder()
                .name("ban")
                .info("ban")
                .locality(Locality.GROUP)
                .privacy(Privacy.ADMIN)
                .action(this::ban)
                .build();
    }

    @SuppressWarnings("unused")
    public Ability checkLinks() {
        return Ability
                .builder()
                .name(DEFAULT)
                .flag(TEXT)
                .locality(Locality.GROUP)
                .privacy(Privacy.PUBLIC)
                .action(this::checkLinks)
                .build();
    }

    private void ban(MessageContext ctx) {
        var message = ctx.update().getMessage();
        var bannee = message.getReplyToMessage().getFrom();
        var chatId = message.getChatId().toString();

        try {
            execute(new BanChatMember(chatId, bannee.getId()));
            silent.execute(new DeleteMessage(chatId, message.getMessageId()));
        } catch (TelegramApiException e) {
            log.error("Error", e);
        }
    }

    private void checkLinks(MessageContext ctx) {
        var message = ctx.update().getMessage();
        var sender = message.getFrom();
        long senderId = sender.getId();
        var chatId = message.getChatId().toString();

        if (isAdmin(senderId)) return;
        if (isGroupAdmin(ctx.update(), senderId)) return;
        if (Boolean.TRUE.equals(sender.getIsPremium())) return;
        if (isUserKnown(chatId, senderId)) return;

        var hasLink = message.getText().matches("https?://.*");

        if (!hasLink) return;

        var messageText = "Yo nibba, you triggered an anti spam thingy. " +
                "No links are allowed if you're not in the chat group. " +
                "Contact the admins if you're not a bot.";
        var senderTag = !sender.getUserName().isEmpty() ? ("@" + sender.getUserName() + " ") : "";
        var replyMessage = silent.forceReply(senderTag + messageText, message.getChatId());
        silent.execute(new BanChatMember(chatId, senderId));
        replyMessage.ifPresent(msg ->
                silent.execute(new DeleteMessage(chatId, msg.getMessageId())));
    }

    private boolean isUserKnown(String chatId, long userId) {
        var status = getStatus(chatId, userId);
        if ("left".equalsIgnoreCase(status)) return false;
        return !NONE.equalsIgnoreCase(status);
    }

    private String getStatus(String chatId, long userId) {
        ChatMember chatMember;
        try {
            chatMember = execute(new GetChatMember(chatId, userId));
            if (chatMember == null) return NONE;
            return chatMember.getStatus();
        } catch (TelegramApiException e) {
            if (e.getMessage().contains("[400] Bad Request: user not found")) {
                return NONE;
            }
            throw new RuntimeException(e);
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
}
