package dev.haarolean.gnutardbot.util;

import org.mapdb.DB;
import org.telegram.abilitybots.api.db.MapDBContext;

import java.util.Set;

import static org.telegram.abilitybots.api.bot.BaseAbilityBot.ADMINS;

public class AdminDatabaseAdapter extends MapDBContext {
    private final Set<Long> admins;

    public AdminDatabaseAdapter(DB db, Set<Long> admins) {
        super(db);
        this.admins = admins;
    }

    @Override
    @SuppressWarnings("unchecked") // ugly hack but whatevs
    public <T> Set<T> getSet(String name) {
        if (ADMINS.equalsIgnoreCase(name)) {
            return (Set<T>) admins;
        }
        return super.getSet(name);
    }
}
