package com.arsenarsen.lavaplayerbridge.hooks;

import com.arsenarsen.lavaplayerbridge.player.Item;
import com.arsenarsen.lavaplayerbridge.player.Player;

/**
 * Called when calling {@link Player#queue}.
 */
@FunctionalInterface
public interface QueueHook extends Hook {
    void execute(Player player, Item item);
}
