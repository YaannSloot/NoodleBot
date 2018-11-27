package com.arsenarsen.lavaplayerbridge.hooks;

import com.arsenarsen.lavaplayerbridge.player.Player;

public interface PlayerCreateHook extends Hook {
    void execute(Player player);
}
