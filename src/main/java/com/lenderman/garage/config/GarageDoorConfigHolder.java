//TODO Add formatting
package com.lenderman.garage.config;

import java.io.File;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class GarageDoorConfigHolder
{
    private static Logger log = Logger.getLogger(GarageDoorConfigHolder.class);

    public static GarageDoorConfig garageDoorConfig = loadConfiguration();

    private static GarageDoorConfig loadConfiguration()
    {
        File file = new File("config.yaml");
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        try
        {
            return om.readValue(file, GarageDoorConfig.class);
        }
        catch (Exception ex)
        {
            log.error(
                    "Error loading configuration file, did you create a config.yaml file?",
                    ex);
            System.exit(-1);
            return null;
        }
    }
}