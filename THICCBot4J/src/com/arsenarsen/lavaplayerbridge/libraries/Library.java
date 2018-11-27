package com.arsenarsen.lavaplayerbridge.libraries;

import com.arsenarsen.lavaplayerbridge.player.Provider;

public abstract class Library {
    private Object lib;
    private Library(){}
    protected Library(Object lib){
        this.lib = lib;
    }
    /**
     * Sets a provider for a guild.
     *
     * @param guildId  The guild ID to assign this provider to
     * @param provider The provider to set
     */
    public abstract void setProvider(String guildId, Provider provider);

    /**
     * Checks is a guild ID valid
     * @param guildId The guild ID to check
     * @return True if the guild ID is valid, false otherwise.
     */
    public abstract boolean isValidGuild(String guildId);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Library library = (Library) o;

        return lib.equals(library.lib);
    }

    @Override
    public int hashCode() {
        return lib.hashCode();
    }
}
