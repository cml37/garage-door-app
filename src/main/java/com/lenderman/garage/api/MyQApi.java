/*******************************************************
 * MyQ API Interactions
 *******************************************************/

package com.lenderman.garage.api;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import com.lenderman.garage.GarageDoorConstants;
import com.lenderman.garage.config.GarageDoorConfigHolder;
import com.lenderman.garage.controller.GarageDoorGuiController;
import com.lenderman.garage.types.OpenerCommand;
import com.lenderman.garage.types.OpenerObject;
import com.lenderman.garage.types.OpenerState;
import com.lenderman.garage.utils.MetadataUtils;

/**
 * Procedures for interacting with the MyQ API
 *
 * @author Chris Lenderman
 */
public class MyQApi
{
    // TODO investigate moving API definitions into a JSON file that can be
    // loaded dynamically

    /** Class logger */
    private static Logger log = Logger.getLogger(GarageDoorGuiController.class);

    /** One and only HttpClient reference */
    private static HttpClient client = HttpClientBuilder.create().build();

    /**
     * Login API
     *
     * @return String security token
     */
    public static String login() throws Exception
    {
        HttpPost post = new HttpPost(GarageDoorConstants.POST_LOGIN_URL);

        ArrayList<Header> headers = GarageDoorConstants.generateHeaders(null);
        headers.stream().forEach(header -> post.addHeader(header));
        JSONObject json = MetadataUtils
                .getLoginBody(GarageDoorConfigHolder.garageDoorConfig);
        post.setEntity(new StringEntity(json.toString()));

        HttpResponse response = client.execute(post);

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
        {
            log.debug("Login API request success");
            return new JSONObject(EntityUtils.toString(response.getEntity()))
                    .getString("SecurityToken");
        }
        else
        {
            log.error("Login API request failed.  Status code: "
                    + response.getStatusLine().getStatusCode());
        }
        return null;
    }

    /**
     * Given a security token, retrieve the account ID
     *
     * @param String
     * @return String
     */
    public static String getAccountId(String securityToken) throws Exception
    {
        HttpGet get = new HttpGet(GarageDoorConstants.GET_ACCOUNT_URL);

        ArrayList<Header> headers = GarageDoorConstants
                .generateHeaders(securityToken);
        headers.stream().forEach(header -> get.addHeader(header));
        HttpResponse response = client.execute(get);

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
        {
            JSONObject object = new JSONObject(
                    EntityUtils.toString(response.getEntity()));

            log.debug("Device Account API request success.  JSON response: "
                    + object.toString());

            JSONArray items = object.getJSONArray("Items");
            return items.getJSONObject(0).getString("Id");
        }
        else
        {
            log.error("Device Account API request failed.  Status code: "
                    + response.getStatusLine().getStatusCode());
        }
        return null;
    }

    /**
     * API to get details about all openers
     *
     * @return ArrayList<OpenerObject>
     */
    public static ArrayList<OpenerObject> getDetails(String securityToken)
            throws Exception
    {
        ArrayList<OpenerObject> openers = new ArrayList<OpenerObject>();

        HttpGet get = new HttpGet(GarageDoorConstants
                .generateGetDevicesUrl(getAccountId(securityToken)));

        ArrayList<Header> headers = GarageDoorConstants
                .generateHeaders(securityToken);
        headers.stream().forEach(header -> get.addHeader(header));
        HttpResponse response = client.execute(get);

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
        {
            JSONObject object = new JSONObject(
                    EntityUtils.toString(response.getEntity()));

            log.debug("Device Details API request success.  JSON response: "
                    + object.toString());

            JSONArray devices = object.getJSONArray("items");
            for (int index = 0; index < devices.length(); index++)
            {
                JSONObject device = devices.getJSONObject(index);
                if (device.getString("device_family").equals("garagedoor"))
                {
                    OpenerObject opener = new OpenerObject();
                    opener.name = device.getString("name");
                    opener.serialNumber = device.getString("serial_number");
                    JSONObject state = device.getJSONObject("state");
                    opener.state = OpenerState
                            .findByValue(state.getString("door_state"));
                    opener.stateUpdatedTime = LocalDateTime
                            .parse(state.getString("last_update").substring(0,
                                    state.getString("last_update")
                                            .indexOf(".")))
                            .atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
                    openers.add(opener);
                }
            }
        }
        else
        {
            log.error("Device Details API request failed.  Status code: "
                    + response.getStatusLine().getStatusCode());
        }
        return openers;
    }

    /**
     * API to change state of the garage door opener
     *
     * @param String the opener serial number
     * @param OpenerCommand the command to execute
     * @param String SecurityToken
     * @return boolean success
     */
    public static boolean changeState(String serialNumber,
            OpenerCommand command, String securityToken) throws Exception
    {
        HttpPut put = new HttpPut(GarageDoorConstants.generatePutDeviceUrl(
                getAccountId(securityToken), serialNumber));
        ArrayList<Header> headers = GarageDoorConstants
                .generateHeaders(securityToken);
        headers.stream().forEach(header -> put.addHeader(header));
        JSONObject json = new JSONObject();
        json.put("action_type", command.getCommand());
        put.setEntity(new StringEntity(json.toString()));

        HttpResponse response = client.execute(put);

        if (response.getStatusLine()
                .getStatusCode() == HttpStatus.SC_NO_CONTENT)
        {
            return true;
        }
        else
        {
            log.error("Device Attributes API request failed.  Status code: "
                    + response.getStatusLine().getStatusCode());
        }

        return false;
    }
}