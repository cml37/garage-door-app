/*******************************************************
 * Garage Door Duration Utils
 *******************************************************/
package com.lenderman.garage.utils;

/**
 * Utils for determining duration
 *
 * @author Chris Lenderman
 */
public class DurationUtils
{
    /**
     * Gets a pretty print version of duration
     * 
     * @param long minutes
     * @return String
     */
    public static String getDurationPrettyPrint(long minutes)
    {
        long hoursBase = minutes / 60;
        long minutesBase = minutes % 60;
        long daysBase = minutes / 1440;

        if (minutes < 1)
        {
            return "less than a minute";
        }
        else if (minutes < 60)
        {
            return minutes + (minutes == 1 ? " minute" : " minutes");
        }
        else if (minutes < 240)
        {
            return hoursBase + (hoursBase == 1 ? " hour, " : " hours, ")
                    + minutesBase + (minutesBase == 1 ? " minute" : " minutes");
        }
        else if (minutes < 1440)
        {
            return hoursBase + (hoursBase == 1 ? " hour" : " hours");
        }
        else
        {
            return daysBase + (daysBase == 1 ? " day" : " days");
        }
    }
}