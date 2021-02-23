/*******************************************************
 * Garage Door Action Callback Interface
 *******************************************************/
package com.lenderman.garage.callback;

public interface GarageDoorActionCallback
{
    /**
     * Called back on garage door action change
     */
    public void onGarageDoorActionChange(String serialNumber);
}