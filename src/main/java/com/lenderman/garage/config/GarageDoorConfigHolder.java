/*******************************************************
 * Garage Door Config Holder
 *******************************************************/
package com.lenderman.garage.config;

import java.io.File;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lenderman.garage.GarageDoorConstants;

/**
 * Configuration holder for program configuration.
 *
 * @author Chris Lenderman
 */
public class GarageDoorConfigHolder
{
    /** Class logger */
    private static Logger log = Logger.getLogger(GarageDoorConfigHolder.class);

    /** Application Config Instance */
    public static GarageDoorConfig garageDoorConfig = loadConfiguration();

    /**
     * Method to load configuration
     */
    private static GarageDoorConfig loadConfiguration()
    {
        File file = new File(GarageDoorConstants.CONFIG_FILE_NAME);
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        try
        {
            return om.readValue(file, GarageDoorConfig.class);
        }
        catch (Exception ex)
        {
            log.error(
                    "Error loading configuration file, did you create a + " GarageDoorConstants.CONFIG_FILE_NAME + " file?",
                    ex);
            System.exit(-1);
            return null;
        }
    }
}