/*******************************************************
 * Notification Utils
 *******************************************************/
package com.lenderman.garage.utils;

import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.event.MouseListener;
import com.lenderman.garage.GarageDoorConstants;

/**
 * Utilities to support notifications.
 *
 * @author Chris Lenderman
 */
public class NotificationUtils
{
    /** The tray icon */
    private static TrayIconWrapper trayIcon = null;

    /** Static init block */
    static
    {
        if (TrayIconWrapper.trayIconSupported())
        {
            trayIcon = new TrayIconWrapper(
                    GarageDoorConstants.GARAGE_ICON_SMALL.getImage());

            trayIcon.setImageAutoSize(true);
            SystemTrayWrapper.add(trayIcon);
        }
    }

    /**
     * Adds a tray icon mouse listener
     *
     * @param MouseListener
     */
    public static void addTrayIconMouseListener(MouseListener listener)
    {
        if (trayIcon != null)
        {
            trayIcon.addMouseListener(listener);
        }
    }

    /**
     * Sets the tray icon tip text
     *
     * @param String
     */
    public static void setTrayIconTipText(String text)
    {
        if (trayIcon != null)
        {
            trayIcon.setToolTip(text);
        }
    }

    /**
     * Sets the tray icon popup menu
     *
     * @param PopupMenu
     */
    public static void setTrayIconPopupMenu(PopupMenu menu)
    {
        trayIcon.setPopupMenu(menu);
    }

    /**
     * Sets the tray icon image
     *
     * @param Image
     */
    public static void setTrayIconImage(Image image)
    {
        if (trayIcon != null)
        {
            trayIcon.setImage(image);
        }
    }
}