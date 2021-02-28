/*******************************************************
 * Garage Door Config
 *******************************************************/
package com.lenderman.garage.config;

/**
 * Configuration settings for the application. This POJO is intended to be
 * serialized for saving settings.
 *
 * @author Chris Lenderman
 */
public class GarageDoorConfig
{
    /** Indicates whether IRC is enabled */
    private boolean ircEnabled;

    /** The IRC nickname to be used by the IRC Chat Bot */
    private String ircNickname;

    /** The IRC server address to be used by the IRC Chat Bot */
    private String ircServerAddress;

    /** The IRC channel to be used by the IRC Chat Bot */
    private String ircChannel;

    /**
     * The default name to respond to when the requesting IRC user's name is not
     * known
     */
    private String ircDefaultSenderNickname;

    /** The MyQ Username */
    private String myQUsername;

    /** The MyQ Password */
    private String myQPassword;

    public boolean isIrcEnabled()
    {
        return ircEnabled;
    }

    public void setIrcEnabled(boolean ircEnabled)
    {
        this.ircEnabled = ircEnabled;
    }

    public String getIrcNickname()
    {
        return ircNickname;
    }

    public void setIrcNickname(String ircNickname)
    {
        this.ircNickname = ircNickname;
    }

    public String getIrcServerAddress()
    {
        return ircServerAddress;
    }

    public void setIrcServerAddress(String ircServerAddress)
    {
        this.ircServerAddress = ircServerAddress;
    }

    public String getIrcChannel()
    {
        return ircChannel;
    }

    public void setIrcChannel(String ircChannel)
    {
        this.ircChannel = ircChannel;
    }

    public String getIrcDefaultSenderNickname()
    {
        return ircDefaultSenderNickname;
    }

    public void setIrcDefaultSenderNickname(String ircDefaultSenderNickname)
    {
        this.ircDefaultSenderNickname = ircDefaultSenderNickname;
    }

    public String getMyQUsername()
    {
        return myQUsername;
    }

    public void setMyQUsername(String myQUsername)
    {
        this.myQUsername = myQUsername;
    }

    public String getMyQPassword()
    {
        return myQPassword;
    }

    public void setMyQPassword(String myQPassword)
    {
        this.myQPassword = myQPassword;
    }

}