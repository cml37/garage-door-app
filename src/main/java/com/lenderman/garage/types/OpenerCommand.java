/*******************************************************
 * Garage Door Opener Command Enumeration
 *******************************************************/
package com.lenderman.garage.types;

/**
 * Enumeration representing the garage door opener command
 *
 * @author Chris Lenderman
 */
public enum OpenerCommand
{
    CLOSE("close"), OPEN("open");

    /** Enumeration value */
    String command;

    /**
     * @return value of enumeration
     */
    public String getCommand()
    {
        return command;
    }

    /**
     * Constructor
     */
    OpenerCommand(String command)
    {
        this.command = command;
    }
}