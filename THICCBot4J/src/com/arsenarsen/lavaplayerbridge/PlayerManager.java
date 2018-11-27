package com.arsenarsen.lavaplayerbridge;

import com.arsenarsen.lavaplayerbridge.hooks.HookManager;
import com.arsenarsen.lavaplayerbridge.hooks.PlayerCreateHook;
import com.arsenarsen.lavaplayerbridge.libraries.Library;
import com.arsenarsen.lavaplayerbridge.player.Player;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("ALL")
public class PlayerManager {

    private final Library lib;
    private static Map<Library, PlayerManager> playerManagerMap = new ConcurrentHashMap<>();
    private Map<String, Player> players = new ConcurrentHashMap<>();
    private volatile AudioPlayerManager manager = new DefaultAudioPlayerManager();
    private HookManager<PlayerCreateHook> playerCreateHooks = new HookManager<>();

    private PlayerManager() {
        lib = null;
    } // Java wants finals to be declared in both constructors

    private PlayerManager(Library lib) {
        this.lib = lib;
    }

    /**
     * Gets a player for a guild from the internal cache. If a player is missing it will make it.
     * The guildId will be checked first.
     *
     * @param lib     A discord client provided from LibraryFactory.
     * @param guildId ID of the Guild this player is for.
     * @return A Player for that guild, instances are cached.
     * @throws IllegalArgumentException if the guild ID is invalid
     */
    public static Player getPlayer(Library lib, String guildId) {
        return getPlayerManager(lib).getPlayer(guildId);
    }

    /**
     * Deletes a player manager. Use after logging a bot out or dropping a library.
     *
     * @param library The {@link Library} whose player manager to drop.
     * @return The deleted player manager, or null if there was no in the first place.
     */
    public static PlayerManager deletePlayerManager(Library library) {
        return playerManagerMap.remove(library);
    }

    /**
     * @return The Player create hook manager.
     */
    public HookManager<PlayerCreateHook> getPlayerCreateHooks() {
        return playerCreateHooks;
    }

    /**
     * Adds a player create hook to the current PlayerManager.
     * <br>
     * Use {@link PlayerManager#getPlayerCreateHooks()} instead, Deprecated.
     *
     * @param hook The hook to register
     * @return True, as specified per {@link List#add(Object)}
     */
    @Deprecated
    public boolean registerHook(PlayerCreateHook hook) {
        return getPlayerCreateHooks().register(hook);
    }

    /**
     * Gets a player associated with this PlayerManager.
     *
     * @param guildId The Guild ID to get the player for.
     * @return A new player or a player from internal cache.
     */
    public Player getPlayer(String guildId) {
        if (!lib.isValidGuild(guildId)) {
            players.remove(guildId);
            throw new IllegalArgumentException("Guild ID must be valid!");
        }
        return players.computeIfAbsent(guildId, guild -> {
            Player player = new Player(this, manager.createPlayer(), manager, guild);
            playerCreateHooks.forEach(hook -> hook.execute(player));
            return player;
        });
    }

    /**
     * @return All the {@link Player} instances associated with this {@link PlayerManager}
     */
    public Collection<Player> getPlayers() {
        Iterator<Player> play = players.values().iterator();
        while (play.hasNext()) {
            if (!lib.isValidGuild(play.next().getGuildId()))
                play.remove();
        }
        return players.values();
    }

    /**
     * Gets the {@link AudioPlayerManager} used.
     *
     * @return {@link AudioPlayerManager} currently in use.
     */
    public AudioPlayerManager getManager() {
        return manager;
    }

    /**
     * Sets the used {@link AudioPlayerManager}. Throws {@link IllegalStateException} if a player was already made in this manager.
     *
     * @param manager The manager to use.
     * @throws IllegalStateException    if a player was already created.
     * @throws IllegalArgumentException if manager is null.
     */
    public void setManager(AudioPlayerManager manager) {
        if (!players.isEmpty() && manager != this.manager)
            throw new IllegalStateException("Attempt to assign a AudioPlayerManager after a player has been created");
        if (manager == null)
            // Lower-cased here because of the param name. Don't witch-hunt me
            //                                  V - There
            throw new IllegalArgumentException("manager must not be null!");
        this.manager = manager;
    }

    /**
     * Gets a player manager fora library
     *
     * @param library The library to make the player for
     * @return A new or a cached library.
     */
    public static PlayerManager getPlayerManager(Library library) {
        return playerManagerMap.computeIfAbsent(library, PlayerManager::new);
    }

    /**
     * Returns the library associated with this {@link PlayerManager}.
     *
     * @return The library associated.
     */
    public Library getLibrary() {
        return lib;
    }

    /**
     * Checks does a {@link Player} exist
     *
     * @param id The guild ID to check for
     * @return True if the player exists
     */
    public boolean hasPlayer(String id) {
        if (!lib.isValidGuild(id)) {
            players.remove(id);
        }
        return players.containsKey(id);
    }

    /**
     * Deletes a certain player from the cache.
     *
     * @param player The player to delete. It will immediately stop working.
     */
    public void deletePlayer(Player player) {
        players.remove(player.getGuildId());
        player.crash();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerManager that = (PlayerManager) o;

        if (!Objects.equals(lib, that.lib)) return false;
        if (!players.equals(that.players)) return false;
        return manager.equals(that.manager);
    }

    @Override
    public int hashCode() {
        int result = lib != null ? lib.hashCode() : 0;
        result = 31 * result + manager.hashCode();
        return result;
    }
}
