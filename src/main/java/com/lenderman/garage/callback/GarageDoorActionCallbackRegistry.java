/*******************************************************
 * Garage Door Action Registry
 *******************************************************/
package com.lenderman.garage.callback;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A registry of callbacks for listeners interested in changes induced through
 * garage door actions
 *
 * @author Chris Lenderman
 */
public class GarageDoorActionCallbackRegistry
{
    /** List of registered garage door action callbacks */
    private static CopyOnWriteArrayList<GarageDoorActionCallback> garageDoorActionCallbacks = new CopyOnWriteArrayList<GarageDoorActionCallback>();

    /**
     * Registers a callback
     */
    public static void registerActionCallback(GarageDoorActionCallback callback)
    {
        garageDoorActionCallbacks.add(callback);
    }

    /**
     * Unregisters a callback
     */
    public static void unregisterActionCallback(
            GarageDoorActionCallback callback)
    {
        garageDoorActionCallbacks.remove(callback);
    }

    /**
     * Notifies all callback
     */
    public static void notifyGarageDoorActionStatusChange(String serialNumber)
    {
        garageDoorActionCallbacks.forEach(
                callback -> callback.onGarageDoorActionChange(serialNumber));
    }
}