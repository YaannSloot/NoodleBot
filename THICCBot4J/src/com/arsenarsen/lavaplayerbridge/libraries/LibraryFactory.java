package com.arsenarsen.lavaplayerbridge.libraries;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Makes {@link Library} objects.
 */
public class LibraryFactory {
    /**
     * Creates a library for a given object. Looks for bindings in the package com.arsenarsen.lavaplayerbridge.bindings
     * @param library The library to bind.
     * @return The library after the binding is made.
     * @throws UnknownBindingException If binding library creation failed.
     */
    public static Library getLibrary(Object library) throws UnknownBindingException {
        Library ret;
        try {
            Class<?> c = Class.forName("com.arsenarsen.lavaplayerbridge.bindings.Binding"+ library.getClass().getSimpleName());
            Method m = c.getMethod("createLibrary", Object.class);
            m.setAccessible(true);
            return (Library) m.invoke(null, library);
        } catch (ClassCastException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new UnknownBindingException("Could not create a Library object for " + library, e, library);
        }
    }
}
