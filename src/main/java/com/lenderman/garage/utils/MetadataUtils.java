/*******************************************************
 * Metadata Utils
 *******************************************************/
package com.lenderman.garage.utils;

import java.util.ArrayList;
import java.util.stream.Collectors;
import com.lenderman.garage.api.MyQApiController;
import com.lenderman.garage.types.OpenerObject;

/**
 * Utilities to support retrieving metadata about the garage door opener
 *
 * @author Chris Lenderman
 */
public class MetadataUtils
{
    /**
     * Given a garage door serial number, return the name
     */
    public static String getSerialNumberForName(String name)
    {
        try
        {
            ArrayList<OpenerObject> openers = MyQApiController
                    .getGarageDoorDetails();
            return openers.stream().filter(o -> o.name.equals(name))
                    .collect(Collectors.toList()).get(0).serialNumber;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}