/*******************************************************
 * Garage Door Constants
 *******************************************************/
package com.lenderman.garage;

import java.util.ArrayList;
import javax.swing.ImageIcon;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

/**
 * Constants used by the application
 *
 * @author Chris Lenderman
 */
public class GarageDoorConstants
{
    /** Configuration file name */
    public static final String CONFIG_FILE_NAME = "config.yaml";

    /** icon used for system tray, etc. */
    public static final ImageIcon GARAGE_ICON_SMALL = new ImageIcon(
            GarageDoorConstants.class.getClassLoader().getResource(
                    "com/lenderman/garage/images/garage-icon-white.png"));

    /**
     * Number of status update intervals to execute on garage door state change
     */
    public static final int STATUS_UPDATE_INTERVALS_TO_EXECUTE = 10;

    /**
     * Amount of time to wait between each status interval update (in
     * milliseconds)
     */
    public static final int STATUS_UPDATE_INTERVALS_TIME_PERIOD_DELAY_MS = 3000;

    /** Application ID */
    public static String APPLICATION_ID = "JVM/G9Nwih5BwKgNCjLxiFUQxQijAebyyg8QUHr7JOrP+tuPb8iHfRHKwTmDzHOu";

    /** API URLs */
    private static String BASE_URL = "https://api.myqdevice.com";
    public static String GET_ACCOUNT_URL = BASE_URL + "/api/v5/accounts";
    public static String POST_LOGIN_URL = BASE_URL + "/api/v5/Login";

    public static String generateGetDevicesUrl(String myQAccountId)
    {
        return BASE_URL + "/api/v5.1/Accounts/" + myQAccountId + "/Devices";
    }

    public static String generatePutDeviceUrl(String myQAccountId,
            String serialNumber)
    {
        return BASE_URL + "/api/v5.1/Accounts/" + myQAccountId + "/Devices/"
                + serialNumber + "/actions";
    }

    /**
     * Generates headers for use by the API
     *
     * @param String securityToken
     * @return ArrayList<Header>
     */
    public static ArrayList<Header> generateHeaders(String securityToken)
    {
        ArrayList<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Content-Type", "application/json"));
        headers.add(new BasicHeader("MyQApplicationId",
                GarageDoorConstants.APPLICATION_ID));

        if (securityToken != null)
        {
            headers.add(new BasicHeader("SecurityToken", securityToken));
        }

        return headers;
    }
}