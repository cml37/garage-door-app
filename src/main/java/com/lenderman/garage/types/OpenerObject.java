/*******************************************************
 * Garage Door Opener Object
 *******************************************************/
package com.lenderman.garage.types;

/**
 * A glorified struct for aggregating garage door opener information.
 *
 * @author Chris Lenderman
 */
public class OpenerObject
{
    public String serialNumber;
    public String name;
    public OpenerState state;
    public Long stateUpdatedTime;
}