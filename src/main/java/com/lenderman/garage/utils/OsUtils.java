/*******************************************************
 * OS Utilities
 *******************************************************/
package com.lenderman.garage.utils;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import com.lenderman.garage.GarageDoorConstants;

/**
 * Utilities for cross-platform OS support.
 *
 * @author Chris Lenderman
 */
public class OsUtils
{
    /** Class logger */
    private static Logger log = Logger.getLogger(OsUtils.class);

    /**
     * Determines if this computer is running a Linux OS
     *
     * @return boolean true if running Linux OS
     */
    public static boolean isLinuxOS()
    {
        String os = System.getProperty("os.name");
        return ((os != null) && os.toLowerCase().startsWith("linux"));
    }

    /**
     * Determines if this computer is running a windows OS
     *
     * @return boolean true if running Windows OS
     */
    public static boolean isWindowsOS()
    {
        String os = System.getProperty("os.name");
        return ((os != null) && os.toLowerCase().startsWith("win"));
    }

    /**
     * Determines if this computer is running a mac OS
     *
     * @return boolean true if running mac OS
     */
    public static boolean isMacOS()
    {
        String os = System.getProperty("os.name");
        return ((os != null) && os.toLowerCase().startsWith("mac"));
    }

    /**
     * Returns the dialog icon for use on this operating system. Mac OS X needs
     * a higher resolution to ensure that we have a good icon in the dock.
     *
     * @return Image
     */
    public static Image getDialogIcon()
    {
        if (isMacOS())
        {
            // TODO higher resolution Mac icon
            return GarageDoorConstants.GARAGE_ICON_SMALL.getImage();
        }
        else
        {
            return GarageDoorConstants.GARAGE_ICON_SMALL.getImage();
        }
    }

    /**
     * Gets the directory which contains the running JAR file
     *
     * @param Class the class to assess
     * @return File representing the JAR directory
     */
    public static File getJarDir(Class<?> aclass)
    {
        URL url;
        String extURL;

        // get an url
        try
        {
            url = aclass.getProtectionDomain().getCodeSource().getLocation();
        }
        catch (SecurityException ex)
        {
            url = aclass.getResource(aclass.getSimpleName() + ".class");
        }

        // convert to external form
        extURL = url.toExternalForm();

        // prune for various cases
        if (extURL.endsWith(".jar"))
        {
            extURL = extURL.substring(0, extURL.lastIndexOf("/"));
        }
        else
        { // from getResource
            String suffix = "/" + (aclass.getName()).replace(".", "/")
                    + ".class";
            extURL = extURL.replace(suffix, "");
            if (extURL.startsWith("jar:") && extURL.endsWith(".jar!"))
            {
                extURL = extURL.substring(4, extURL.lastIndexOf("/"));
            }
        }

        // convert back to url
        try
        {
            url = new URL(extURL);
        }
        catch (MalformedURLException mux)
        {
            // leave url unchanged; probably does not happen
        }

        // convert url to File
        try
        {
            return new File(url.toURI());
        }
        catch (URISyntaxException ex)
        {
            return new File(url.getPath());
        }
    }

    /**
     * Gets the current path
     *
     * @return String the current path
     */
    public static String getCurrentPath()
    {
        String path = null;
        URL url = ClassLoader.getSystemClassLoader().getResource(".");

        if (url != null)
        {
            try
            {
                path = (URLDecoder.decode(url.getPath(), "UTF-8"));
            }
            catch (UnsupportedEncodingException e)
            {
                log.warn("Couldn't decode current path: " + e);
            }
        }

        if ((path == null) && (getJarDir(OsUtils.class) != null))
        {
            path = getJarDir(OsUtils.class).getAbsolutePath();
        }

        if (path == null)
        {
            path = System.getenv("user.dir");
        }
        return path;
    }

    /**
     * Executes a command line command
     *
     * @param String the command
     * @param HashMap <String, String> a list of substitutions to process
     * @param boolean whether or not to wait for termination before continuing
     *        execution
     * @return if waiting for termination, a list of output strings from the
     *         process.
     * @throws IOException
     */
    public static ArrayList<String> executeCommandLineCommand(String command,
            HashMap<String, String> substitutes, boolean waitForTermination)
            throws IOException
    {
        String regex = "(\"[^\"]*\")|(\\S+)";

        List<String> matchList = new ArrayList<String>();

        Matcher m = Pattern.compile(regex).matcher(command);
        while (m.find())
        {
            if (m.group(1) != null)
            {
                matchList.add(m.group(1));
            }
            else if (m.group(2) != null)
            {
                matchList.add(m.group(2));
            }
        }

        if (substitutes != null)
        {
            for (int index = 0; index < matchList.size(); index++)
            {
                for (String key : substitutes.keySet())
                {
                    if (matchList.get(index).contains(key))
                    {
                        matchList.set(index, matchList.get(index).replace(key,
                                substitutes.get(key)));
                    }
                }
            }
        }

        ProcessBuilder builder = new ProcessBuilder(matchList);
        builder.directory(new File(OsUtils.getCurrentPath()));

        Process p = builder.start();
        if (waitForTermination)
        {
            try
            {
                p.waitFor();

                BufferedReader is = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));

                String line;
                ArrayList<String> results = new ArrayList<String>();
                while ((line = is.readLine()) != null)
                {
                    results.add(line);
                }

                is = new BufferedReader(
                        new InputStreamReader(p.getErrorStream()));
                while ((line = is.readLine()) != null)
                {
                    results.add(line);
                }

                return results;
            }
            catch (InterruptedException e)
            {
                log.info("Command line execution terminated: " + e);
            }
        }
        return null;
    }

    /**
     * Using Java reflection, sets the mac dock image
     *
     * @param Image the image to set
     */
    @SuppressWarnings("all")
    public static void setMacDockImage(Image image)
    {
        if (OsUtils.isMacOS())
        {
            System.setProperty(
                    "com.apple.mrj.application.apple.menu.about.name",
                    "GarageDoor");
            try
            {
                Class<?> c = Class.forName("com.apple.eawt.Application");
                Method method = c.getMethod("getApplication");
                method.setAccessible(true);
                Object o = method.invoke(null, null);

                Method newMethod = o.getClass().getMethod("setDockIconImage",
                        Image.class);
                newMethod.invoke(o, image);
            }
            catch (Exception ex)
            {
                log.error("Couldn't set Mac dock image: " + ex);
            }
        }
    }
}