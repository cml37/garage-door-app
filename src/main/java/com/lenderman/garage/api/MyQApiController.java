/*******************************************************
 * MyQ API Controller
 *******************************************************/
package com.lenderman.garage.api;

import java.util.ArrayList;
import com.lenderman.garage.callback.GarageDoorActionCallbackRegistry;
import com.lenderman.garage.callback.GarageDoorDetailsCallback;
import com.lenderman.garage.types.AccessTokenResponse;
import com.lenderman.garage.types.OpenerCommand;
import com.lenderman.garage.types.OpenerObject;
import com.lenderman.garage.types.OpenerState;

/**
 * Controller class for MyQ API interactions
 *
 * @author Chris Lenderman
 */
public class MyQApiController
{
    /**
     * Retrieves garage door details
     */
    public static ArrayList<OpenerObject> getGarageDoorDetails()
            throws Exception
    {
        AccessTokenResponse accessToken = MyQApi.login();
        if (accessToken != null)
        {
            return MyQApi.getDetails(accessToken);
        }
        return null;
    }

    /**
     * Retrieves garage door details in a callback, non blocking fashion
     */
    public static void getGarageDoorDetails(GarageDoorDetailsCallback callback)
    {
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                ArrayList<OpenerObject> objects = null;
                try
                {
                    objects = getGarageDoorDetails();
                }
                catch (Exception e)
                {
                    // Do nothing
                }
                callback.onGarageDoorDetailsAvailable(objects);
            }
        };
        new Thread(runnable).start();
    }

    /**
     * Switches the opener state
     */
    public static void switchOpenerState(String serialNumber) throws Exception
    {
        boolean changedState = false;

        ArrayList<OpenerObject> openers = MyQApiController
                .getGarageDoorDetails();

        OpenerObject opener = openers.stream()
                .filter(o -> o.serialNumber.equals(serialNumber)).findFirst()
                .orElse(null);
        if (opener != null)
        {
            if (opener.state == OpenerState.CLOSED)
            {
                changedState = MyQApi.changeState(opener.serialNumber,
                        OpenerCommand.OPEN, MyQApi.login());
            }
            else if (opener.state == OpenerState.OPEN)
            {
                changedState = MyQApi.changeState(opener.serialNumber,
                        OpenerCommand.CLOSE, MyQApi.login());
            }
        }

        if (changedState)
        {
            GarageDoorActionCallbackRegistry
                    .notifyGarageDoorActionStatusChange(serialNumber);
        }
    }
}