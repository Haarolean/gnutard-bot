package dev.haarolean.gnutardbot.abilities

import org.telegram.abilitybots.api.objects.Ability

interface AbilityProvider {
    fun buildAbility(): Ability
}
