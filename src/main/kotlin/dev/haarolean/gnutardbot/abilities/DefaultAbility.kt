package dev.haarolean.gnutardbot.abilities

import dev.haarolean.gnutardbot.TardBot
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.objects.*

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class DefaultAbility(bot: TardBot) : AbilityProvider {
    private val abilities: MutableList<AbilityHandler> = mutableListOf(
        TrollingAbility(bot),
        CheckLinksAbility(bot),
        UnpinDiscussionAbility(bot)
    ) //Our "default" abilities

    override fun buildAbility(): Ability {
        return Ability
            .builder()
            .name("default")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action {
                handle(it)
            }
            .build()
    }

    private fun handle(context: MessageContext) {
        abilities
            .stream()
            .filter { it.isApplicable(context) }
            .findFirst()
            .ifPresent { it.handle(context) }
    }
}
