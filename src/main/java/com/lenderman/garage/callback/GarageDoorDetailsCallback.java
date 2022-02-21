/*******************************************************
 * Garage Door Action Callback Interface
 *******************************************************/
package com.lenderman.garage.callback;

import java.util.ArrayList;
import com.lenderman.garage.types.OpenerObject;

public interface GarageDoorDetailsCallback
{
    /**
     * Called back on garage door details available
     */
    public void onGarageDoorDetailsAvailable(ArrayList<OpenerObject> openers);
}