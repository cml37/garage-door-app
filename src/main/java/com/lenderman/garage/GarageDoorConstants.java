/*******************************************************
 * Garage Door Constants
 *******************************************************/
package com.lenderman.garage;

import java.util.ArrayList;
import javax.swing.ImageIcon;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import com.lenderman.garage.types.AccessTokenResponse;

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
    private static final String BASE_URL = "https://api.myqdevice.com";
    public static final String GET_ACCOUNT_URL = BASE_URL + "/api/v5/accounts";
    public static final String LOGIN_BASE_URL = "https://partner-identity.myq-cloud.com";
    public static final String LOGIN_AUTHORIZE_URL = LOGIN_BASE_URL
            + "/connect/authorize";
    public static final String LOGIN_TOKEN_URL = LOGIN_BASE_URL
            + "/connect/token";

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
     * @param AccessTokenResponse tokenResponse
     * @return ArrayList<Header>
     */
    public static ArrayList<Header> generateHeaders(
            AccessTokenResponse tokenResponse)
    {
        ArrayList<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Content-Type", "application/json"));

        if (tokenResponse != null)
        {
            headers.add(new BasicHeader("Authorization",
                    tokenResponse.getTokenType() + " "
                            + tokenResponse.getAccessToken()));
        }

        return headers;
    }
}