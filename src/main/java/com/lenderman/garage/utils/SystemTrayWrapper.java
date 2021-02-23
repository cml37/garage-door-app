/*******************************************************
 * System Tray Wrapper
 *******************************************************/
package com.lenderman.garage.utils;

import java.lang.reflect.Method;
import org.apache.log4j.Logger;

/**
 * This class supports the system tray. It is a compat class since this
 * capability does not exist in Java 1.5.
 *
 * @author Chris Lenderman
 */
@SuppressWarnings("all")
public class SystemTrayWrapper
{
    /** Class logger */
    private static Logger log = Logger.getLogger(SystemTrayWrapper.class);

    /** The alternating color to use */
    private static Object systemTray;

    /** The alternating color to use */
    private static boolean isSupported = false;

    /** static init block */
    static
    {
        try
        {
            Class<?> systemTrayWrapperClass = Class
                    .forName("java.awt.SystemTray");
            Method method = systemTrayWrapperClass.getMethod("isSupported");
            method.setAccessible(true);
            Object trayIsSupported = method.invoke(null, null);

            isSupported = trayIsSupported != null ? (Boolean) trayIsSupported
                    : false;

            if (isSupported)
            {
                Method trayMethod = systemTrayWrapperClass
                        .getMethod("getSystemTray");
                trayMethod.setAccessible(true);
                systemTray = trayMethod.invoke(null, null);
            }
        }
        catch (Exception ex)
        {
            log.warn("Couldn't initialize system tray: " + ex);
        }
    }

    /**
     * Returns true if the system tray is supported
     *
     * @return true if supported
     */
    public static boolean isSupported()
    {
        return isSupported;
    }

    /**
     * Adds a tray icon to the system tray
     *
     * @param TrayIconWrapper tray icon
     */
    public static void add(TrayIconWrapper trayIcon)
    {
        if (systemTray != null)
        {
            try
            {
                Method add = systemTray.getClass().getMethod("add",
                        TrayIconWrapper.trayIconClass);
                add.setAccessible(true);
                add.invoke(systemTray, trayIcon.trayIcon);
            }
            catch (Exception ex)
            {
                log.error("Couldn't add TrayIcon to system tray: " + ex);
            }
        }
    }
}