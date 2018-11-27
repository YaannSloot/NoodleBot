package com.arsenarsen.lavaplayerbridge.hooks;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Manges hook registrations, un registrations, ...
 * @param <T> The {@link Hook}
 */
public class HookManager<T extends Hook> implements Iterable<T> {
    private Set<T> hooks = new HashSet<>();

    /**
     * Registers a hook.
     * @param hook The hook to register.
     * @return Same as {@link Set#add}.
     */
    public boolean register(T hook){
        return hooks.add(hook);
    }

    /**
     * Unregisters a hook.
     * @param hook The hook to register.
     * @return Same as {@link Set#remove(Object)}.
     */
    public boolean unRegister(T hook){
        return hooks.remove(hook);
    }

    @Override
    public Iterator<T> iterator() {
        return hooks.iterator();
    }
}
