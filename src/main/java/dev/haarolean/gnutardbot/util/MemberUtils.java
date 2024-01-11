package dev.haarolean.gnutardbot.util;

import lombok.RequiredArgsConstructor;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@RequiredArgsConstructor
public class MemberUtils {

    private static final String NONE = "none";

    private final AbilityBot bot;

    public boolean isUserKnown(String chatId, long userId) {
        var status = getStatus(chatId, userId);
        if ("left".equalsIgnoreCase(status)) return false;
        return !NONE.equalsIgnoreCase(status);
    }

    public String getStatus(String chatId, long userId) {
        ChatMember chatMember;
        try {
            chatMember = bot.execute(new GetChatMember(chatId, userId));
            if (chatMember == null) return NONE;
            return chatMember.getStatus();
        } catch (TelegramApiException e) {
            if (e.getMessage().contains("[400] Bad Request: user not found")) {
                return NONE;
            }
            throw new RuntimeException(e);
        }
    }

}
