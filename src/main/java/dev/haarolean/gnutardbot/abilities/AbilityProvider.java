package dev.haarolean.gnutardbot.abilities;

import org.telegram.abilitybots.api.objects.Ability;

public interface AbilityProvider {
    Ability buildAbility();

}
