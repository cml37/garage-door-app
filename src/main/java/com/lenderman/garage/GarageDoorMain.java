/*******************************************************
 * Garage Door Main Entry Point
 *******************************************************/
package com.lenderman.garage;

import java.awt.Font;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import org.apache.log4j.Logger;
import com.lenderman.garage.controller.GarageDoorBotController;
import com.lenderman.garage.controller.GarageDoorGuiController;
import com.lenderman.garage.utils.OsUtils;

/**
 * Main entry point for the application
 *
 * @author Chris Lenderman
 */
public class GarageDoorMain
{
    /** Class logger */
    private static Logger log = Logger.getLogger(GarageDoorMain.class);

    /**
     * Set look and feel for the application
     */
    private static void setLookAndFeel()
    {
        try
        {
            if (!OsUtils.isMacOS())
            {
                for (LookAndFeelInfo info : UIManager
                        .getInstalledLookAndFeels())
                {
                    if ("Nimbus".equals(info.getName()))
                    {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("Couldn't set look and feel: " + e);
        }
    }

    /**
     * Main Entry point
     */
    public static void main(String[] args)
    {
        setLookAndFeel();

        if (OsUtils.isLinuxOS() || OsUtils.isMacOS())
        {
            Font defaultFont = new Font("SansSerif", Font.PLAIN, 11);
            UIManager.put("defaultFont", defaultFont);
        }

        new GarageDoorGuiController();
        try
        {
            new GarageDoorBotController();
        }
        catch (Exception ex)
        {
            log.error("Could not connect to IRC server: ", ex);
        }
    }
}