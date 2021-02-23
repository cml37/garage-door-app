/*******************************************************
 * Garage Door Opener State Enumeration
 *******************************************************/
package com.lenderman.garage.types;

import java.util.Arrays;

/**
 * Enumeration representing the garage door opener state
 *
 * @author Chris Lenderman
 */
public enum OpenerState
{
    CLOSED("closed"),
    CLOSING("closing"),
    OPEN("open"),
    OPENING("opening"),
    STOPPED("stopped"),
    TRANSITION("transition"),
    UNKNOWN("unknown");

    /** Enumeration value */
    String value;

    /**
     * @return value of enumeration
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Constructor
     */
    OpenerState(String value)
    {
        this.value = value;
    }

    /**
     * Given a value, return the enumeration
     *
     * @return OpenerStateStatus
     */
    public static OpenerState findByValue(String value)
    {
        return Arrays.stream(values()).filter(v -> v.value.equals(value))
                .findFirst().orElseGet(null);
    }
}