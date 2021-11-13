/*******************************************************
 * MyQ API Login Interactions
 *******************************************************/

package com.lenderman.garage.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lenderman.garage.GarageDoorConstants;
import com.lenderman.garage.config.GarageDoorConfig;
import com.lenderman.garage.types.AccessTokenResponse;

/**
 * Delegate login handler, separated into its own class due to its complexity!
 *
 * @author Chris Lenderman
 * 
 * @author Dan Cunningham (author of openhab-addons-myq-oauth which was heavily
 *         used here)
 */
public class MyQLoginHandler
{
    /** Class logger */
    private static final Logger log = Logger.getLogger(MyQLoginHandler.class);

    /** GSON lowercase parser */
    private static final Gson gsonLowerCase = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    /** Cookie store for usage with the HTTP Client */
    private static CookieStore httpCookieStore = new BasicCookieStore();

    /** An HTTP Client */
    private static HttpClient httpClient = HttpClientBuilder.create()
            .setDefaultCookieStore(httpCookieStore)
            .setRedirectStrategy(new LaxRedirectStrategy()).build();

    /**
     * Perform the long and arduous login process! The end result: an OAuth
     * Token
     *
     * @param GarageDoorConfig configuration
     * @return AccessTokenResponse
     */
    public static AccessTokenResponse login(GarageDoorConfig garageDoorConfig)
    {
        try
        {
            // MyQ can get picky about blocking user agents apparently
            String userAgent = MyQLoginHandler.randomString(5);

            // Make sure we have a fresh session
            httpCookieStore.clear();

            // Generate a code verifier
            String codeVerifier = generateCodeVerifier();

            // Access the login page
            HttpResponse loginPageResponse = getLoginPage(codeVerifier);

            // Load the login page to get cookies and form parameters
            Document loginPage = Jsoup.parse(EntityUtils
                    .toString(loginPageResponse.getEntity(), "UTF-8"));
            Element form = loginPage.select("form").first();
            Element requestToken = loginPage
                    .select("input[name=__RequestVerificationToken]").first();
            Element returnURL = loginPage.select("input[name=ReturnUrl]")
                    .first();

            // Abort if any issues retrieving the form or token
            if (form == null || requestToken == null)
            {
                throw new IOException("Could not load login page");
            }

            // Post our user name and password along with elements from the
            // scraped form
            String action = GarageDoorConstants.LOGIN_BASE_URL
                    + form.attr("action");
            String location = postLoginAndRetrieveLocation(
                    garageDoorConfig.getMyQUsername(),
                    garageDoorConfig.getMyQPassword(), userAgent, action,
                    requestToken.attr("value"), returnURL.attr("value"));
            if (location == null)
            {
                throw new Exception("Could not login with credentials");
            }

            // finally complete the OAuth flow and retrieve a JSON OAuth token
            // response
            return getLoginToken(userAgent, location, codeVerifier);
        }
        catch (Exception ex)
        {
            log.error("Exception encountered processing login:", ex);
        }
        return null;
    }

    /**
     * Get the login page that will provide the cookies needed for login
     *
     * @param String codeVerifier
     * @return HttpResponse
     */
    private static HttpResponse getLoginPage(String codeVerifier)
            throws InterruptedException, ExecutionException, TimeoutException,
            ClientProtocolException, IOException
    {
        try
        {
            HttpPost request = new HttpPost(
                    GarageDoorConstants.LOGIN_AUTHORIZE_URL);
            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("client_id",
                    GarageDoorConstants.CLIENT_ID));
            urlParameters.add(new BasicNameValuePair("code_challenge",
                    generateCodeChallange(codeVerifier)));
            urlParameters.add(
                    new BasicNameValuePair("code_challenge_method", "S256"));
            urlParameters.add(new BasicNameValuePair("redirect_uri",
                    GarageDoorConstants.REDIRECT_URI));
            urlParameters.add(new BasicNameValuePair("response_type", "code"));
            urlParameters.add(
                    new BasicNameValuePair("scope", GarageDoorConstants.SCOPE));
            request.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse response = httpClient.execute(request);
            return response;
        }
        catch (UnsupportedEncodingException | NoSuchAlgorithmException e)
        {
            throw new ExecutionException(e.getCause());
        }
    }

    /**
     * Get the login page that will provide the cookies needed for login
     *
     * @param String user name
     * @param String password
     * @param String user agent
     * @param String URL
     * @param String request token
     * @param String return URL
     * @return String the URL to follow next
     */
    private static String postLoginAndRetrieveLocation(String username,
            String password, String userAgent, String url, String requestToken,
            String returnURL) throws InterruptedException, ExecutionException,
            TimeoutException, UnsupportedEncodingException, IOException
    {
        /*
         * on a successful post to this page we will get several redirects, and
         * a final 301 to:
         * com.myqops://ios?code=0123456789&scope=MyQ_Residential%
         * 20offline_access&iss=https%3A%2F%2Fpartner-identity. myq-cloud.com
         *
         * We can then take the parameters out of this location and continue the
         * process
         */

        HttpPost request = new HttpPost(url);
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("Email", username));
        urlParameters.add(new BasicNameValuePair("Password", password));
        urlParameters.add(new BasicNameValuePair("__RequestVerificationToken",
                requestToken));
        urlParameters.add(new BasicNameValuePair("ReturnUrl", returnURL));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        request.setHeader("User-Agent", userAgent);

        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(HttpClientContext.COOKIE_STORE,
                httpCookieStore);

        try
        {
            httpClient.execute(request, localContext);
        }
        // We will catch an exception here because the final redirect URI will
        // have a protocol type of com.myqops, which is invalid. Fortunately,
        // the context will have been updated with the redirect URI, which we
        // can snag and return back.
        catch (ClientProtocolException ex)
        {
            URI finalUrl = request.getURI();
            RedirectLocations locations = (RedirectLocations) localContext
                    .getAttribute(HttpClientContext.REDIRECT_LOCATIONS);
            if (locations != null)
            {
                finalUrl = locations.getAll()
                        .get(locations.getAll().size() - 1);
            }
            return finalUrl.toString();
        }
        return null;
    }

    /**
     * Given a user agent, redirect location, and code verifier, get an Access
     * Token
     *
     * @param String user agent
     * @param String redirect location
     * @param String code verifier
     * @return AccessTokenResponse
     */
    private static AccessTokenResponse getLoginToken(String userAgent,
            String redirectLocation, String codeVerifier)
            throws InterruptedException, ExecutionException, TimeoutException,
            ClientProtocolException, IOException
    {
        try
        {
            Map<String, String> params = parseLocationQuery(redirectLocation);

            HttpPost request = new HttpPost(
                    GarageDoorConstants.LOGIN_TOKEN_URL);
            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("client_id",
                    GarageDoorConstants.CLIENT_ID));
            urlParameters.add(new BasicNameValuePair("client_secret",
                    Base64.getEncoder().encodeToString(
                            GarageDoorConstants.CLIENT_SECRET.getBytes())));
            urlParameters
                    .add(new BasicNameValuePair("code", params.get("code")));
            urlParameters
                    .add(new BasicNameValuePair("code_verifier", codeVerifier));
            urlParameters.add(
                    new BasicNameValuePair("grant_type", "authorization_code"));
            urlParameters.add(new BasicNameValuePair("redirect_uri",
                    GarageDoorConstants.REDIRECT_URI));
            urlParameters
                    .add(new BasicNameValuePair("scope", params.get("scope")));

            request.setEntity(new UrlEncodedFormEntity(urlParameters));
            request.setHeader("User-Agent", userAgent);

            HttpContext localContext = new BasicHttpContext();
            localContext.setAttribute(HttpClientContext.COOKIE_STORE,
                    httpCookieStore);

            HttpResponse response = httpClient.execute(request, localContext);

            String loginToken = EntityUtils.toString(response.getEntity(),
                    "UTF-8");

            AccessTokenResponse accessTokenResponse = gsonLowerCase
                    .fromJson(loginToken, AccessTokenResponse.class);

            return accessTokenResponse;
        }
        catch (URISyntaxException e)
        {
            throw new ExecutionException(e.getCause());
        }
    }

    /**
     * Given a length, generate a random string
     *
     * @param int length
     * @return String
     */
    private static String randomString(int length)
    {
        int low = 97; // a-z
        int high = 122; // A-Z
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++)
        {
            sb.append((char) (low
                    + (int) (random.nextFloat() * (high - low + 1))));
        }
        return sb.toString();
    }

    /**
     * Generate a Code Verifier for the MyQ API
     *
     * @return String
     */
    private static String generateCodeVerifier()
            throws UnsupportedEncodingException
    {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(codeVerifier);
    }

    /**
     * Generate a Code Challenge String based on the code verifier
     *
     * @param String code verifier
     * @return String
     */
    private static String generateCodeChallange(String codeVerifier)
            throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        byte[] bytes = codeVerifier.getBytes("US-ASCII");
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(bytes, 0, bytes.length);
        byte[] digest = messageDigest.digest();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    /**
     * Parse the location query URL and create a map of parameters
     *
     * @param String location
     * @return Map
     */
    private static Map<String, String> parseLocationQuery(String location)
            throws URISyntaxException
    {
        URI uri = new URI(location);
        return Arrays.stream(uri.getQuery().split("&"))
                .map(str -> str.split("="))
                .collect(Collectors.toMap(str -> str[0], str -> str[1]));
    }
}
