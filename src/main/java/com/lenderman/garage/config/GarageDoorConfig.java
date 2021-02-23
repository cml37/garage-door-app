//TODO add formatting
package com.lenderman.garage.config;

public class GarageDoorConfig
{
    private String ircNickname;
    private String ircServerAddress;
    private String ircChannel;
    private String ircDefaultSenderNickname;

    private String myQUsername;
    private String myQPassword;

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