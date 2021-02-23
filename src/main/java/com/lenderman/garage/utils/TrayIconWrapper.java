/*******************************************************
 * Tray Icon Wrapper
 *******************************************************/
package com.lenderman.garage.utils;

import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.event.MouseListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.apache.log4j.Logger;

/**
 * This class supports the system tray icon. It is a compat class since this
 * capability does not exist in Java 1.5.
 *
 * @author Chris Lenderman
 */
@SuppressWarnings("all")
public class TrayIconWrapper
{
    /** Class logger */
    private static Logger log = Logger.getLogger(TrayIconWrapper.class);

    /** The tray icon wrapped by this class */
    public Object trayIcon;

    /** The tray icon class */
    public static Class<?> trayIconClass = null;

    /** Static init block */
    static
    {
        // For now, we are not going to support system tray icon on Linux
        if (!OsUtils.isLinuxOS())
        {
            try
            {
                trayIconClass = Class.forName("java.awt.TrayIcon");
            }
            catch (Exception ex)
            {
                log.warn("Couldn't find TrayIcon class: " + ex);
            }
        }
    }

    /**
     * Constructor
     */
    public TrayIconWrapper(Image image)
    {
        if (trayIconSupported())
        {
            try
            {
                Constructor<?> c = trayIconClass
                        .getDeclaredConstructor(Image.class);
                trayIcon = c.newInstance(image);
            }
            catch (Exception ex)
            {
                log.warn("Couldn't construct TrayIcon: " + ex);
            }
        }
    }

    /**
     * Whether or not tray icon is supported by this Java runtime
     *
     * @return true if supported
     */
    public static boolean trayIconSupported()
    {
        return trayIconClass != null;
    }

    /**
     * Adds a mouse listener
     *
     * @param MouseListener the listener to add
     */
    public void addMouseListener(MouseListener listener)
    {
        try
        {
            Method addMouseListener = trayIconClass
                    .getMethod("addMouseListener", MouseListener.class);
            addMouseListener.setAccessible(true);
            addMouseListener.invoke(trayIcon, listener);
        }
        catch (Exception ex)
        {
            log.error("Couldn't add mouse listener to TrayIcon: " + ex);
        }
    }

    /**
     * Sets the image
     *
     * @param Image the image to set
     */
    public void setImage(Image image)
    {
        try
        {
            Method setImage = trayIconClass.getMethod("setImage", Image.class);
            setImage.setAccessible(true);
            setImage.invoke(trayIcon, image);
        }
        catch (Exception ex)
        {
            log.error("Couldn't set image for TrayIcon: " + ex);
        }
    }

    /**
     * Sets the popup menu
     *
     * @param PopupMenu the PopupMenu to set
     */
    public void setPopupMenu(PopupMenu menu)
    {
        try
        {
            Method setPopupMenu = trayIconClass.getMethod("setPopupMenu",
                    PopupMenu.class);
            setPopupMenu.setAccessible(true);
            setPopupMenu.invoke(trayIcon, menu);
        }
        catch (Exception ex)
        {
            log.error("Couldn't set popup menu for TrayIcon: " + ex);
        }
    }

    /**
     * Sets the tool tip
     *
     * @param String the tool tip to set
     */
    public void setToolTip(String tip)
    {
        try
        {
            Method setToolTip = trayIconClass.getMethod("setToolTip",
                    String.class);
            setToolTip.setAccessible(true);
            setToolTip.invoke(trayIcon, tip);
        }
        catch (Exception ex)
        {
            log.error("Couldn't set tool tip for TrayIcon: " + ex);
        }
    }

    /**
     * Sets the image auto size
     *
     * @param boolean true if image should be auto sized
     */
    public void setImageAutoSize(boolean autoSize)
    {
        try
        {
            Method setImageAutoSize = trayIconClass
                    .getMethod("setImageAutoSize", boolean.class);
            setImageAutoSize.setAccessible(true);
            setImageAutoSize.invoke(trayIcon, autoSize);
        }
        catch (Exception ex)
        {
            log.error("Couldn't set image auto size for TrayIcon: " + ex);
        }
    }

    /**
     * Display a balloon information message
     *
     * @param String the title to display
     * @param String the message to display
     * @return true if successful, false otherwise
     */
    public boolean displayMessage(String title, String message)
    {
        try
        {
            Class<?> mt = Class.forName("java.awt.TrayIcon$MessageType");

            Method displayMessage = trayIconClass.getMethod("displayMessage",
                    String.class, String.class, mt);
            displayMessage.setAccessible(true);
            displayMessage.invoke(trayIcon, title, message,
                    Enum.valueOf((Class<Enum>) mt, "INFO"));
            return true;
        }
        catch (Exception ex)
        {
            log.error("Couldn't display message for TrayIcon: " + ex);
        }
        return false;
    }
}