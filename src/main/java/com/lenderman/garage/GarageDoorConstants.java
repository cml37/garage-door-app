/*******************************************************
 * Garage Door Constants
 *******************************************************/
package com.lenderman.garage;

import java.util.ArrayList;
import javax.swing.ImageIcon;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import com.lenderman.garage.types.AccessTokenResponse;
import com.lenderman.garage.types.OpenerCommand;

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

    /** OAuth related fields */
    public static final String CLIENT_SECRET = "VUQ0RFhuS3lQV3EyNUJTdw==";
    public static final String CLIENT_ID = "IOS_CGI_MYQ";
    public static final String REDIRECT_URI = "com.myqops://ios";
    public static final String SCOPE = "MyQ_Residential offline_access";

    /** API URLs */
    private static final String DEVICES_BASE_URL = "https://devices.myq-cloud.com";
    private static final String DEVICES_GARAGE_DOOR_BASE_URL = "https://account-devices-gdo.myq-cloud.com/";
    public static final String GET_ACCOUNT_URL = "https://accounts.myq-cloud.com/api/v6.0/accounts";
    public static final String LOGIN_BASE_URL = "https://partner-identity.myq-cloud.com";
    public static final String LOGIN_AUTHORIZE_URL = LOGIN_BASE_URL
            + "/connect/authorize";
    public static final String LOGIN_TOKEN_URL = LOGIN_BASE_URL
            + "/connect/token";

    /**
     * Generates a GET devices URL to get a list of devices
     *
     * @param String myQAccountId
     * @return String
     */
    public static String generateGetDevicesUrl(String myQAccountId)
    {
        return DEVICES_BASE_URL + "/api/v5.2/Accounts/" + myQAccountId
                + "/Devices";
    }

    /**
     * Generates a PUT device URL to command a device
     *
     * @param String myQAccountId
     * @param String serialNumber
     * @param OpenerCommand command
     * @return String
     */
    public static String generatePutDeviceCommandUrl(String myQAccountId,
            String serialNumber, OpenerCommand command)
    {
        return DEVICES_GARAGE_DOOR_BASE_URL + "/api/v5.2/Accounts/"
                + myQAccountId + "/door_openers/" + serialNumber + "/"
                + command.getCommand();
    }

    /**
     * Generates headers for use by the API
     *
     * @param AccessTokenResponse tokenResponse
     * @return ArrayList<Header>
     */
    public static ArrayList<Header> generateHeaders(
            AccessTokenResponse tokenResponse)
    {
        ArrayList<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("User-Agent", "null"));

        if (tokenResponse != null)
        {
            headers.add(new BasicHeader("Authorization",
                    tokenResponse.getTokenType() + " "
                            + tokenResponse.getAccessToken()));
        }

        return headers;
    }
}