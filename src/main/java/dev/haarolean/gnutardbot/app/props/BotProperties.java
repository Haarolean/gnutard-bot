package dev.haarolean.gnutardbot.app.props;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@ConfigurationProperties("bot")
@Configuration
@Validated
@Data
public class BotProperties {

    @NotEmpty
    private String token;
    private String chatId;
    private Set<Long> admins;

}
