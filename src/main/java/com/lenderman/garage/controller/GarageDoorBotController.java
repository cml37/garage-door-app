/*******************************************************
 * Garage Door Bot Controller
 *******************************************************/
package com.lenderman.garage.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import org.jibble.pircbot.PircBot;
import com.google.common.base.Splitter;
import com.lenderman.garage.GarageDoorConstants;
import com.lenderman.garage.api.MyQApiController;
import com.lenderman.garage.callback.GarageDoorActionCallback;
import com.lenderman.garage.callback.GarageDoorActionCallbackRegistry;
import com.lenderman.garage.config.GarageDoorConfigHolder;
import com.lenderman.garage.types.OpenerObject;
import com.lenderman.garage.utils.ExponentialBackoffService;
import com.lenderman.garage.utils.MetadataUtils;

/**
 * Main Bot controller for the application
 *
 * @author Chris Lenderman
 */
public class GarageDoorBotController extends PircBot
        implements GarageDoorActionCallback
{
    /** Class logger */
    private static Logger log = Logger.getLogger(GarageDoorBotController.class);

    /** Single threaded executor service used principally to make API calls */
    private final ExecutorService exService = Executors
            .newSingleThreadExecutor();

    /** Single threaded executor service used principally to initialize IRC */
    private final ExecutorService ircInitializer = Executors
            .newSingleThreadExecutor();

    /**
     * Maps to keep track of channel and sender that maps to a given serial
     * number
     */
    private ConcurrentHashMap<String, String> serialNumberToChannel = new ConcurrentHashMap<String, String>();
    private ConcurrentHashMap<String, String> serialNumberToSender = new ConcurrentHashMap<String, String>();

    /**
     * Constructor
     */
    public GarageDoorBotController()
    {
        initialize();
    }

    /**
     * Initializes the Controller
     */
    private void initialize()
    {
        ircInitializer.submit(new Runnable()
        {
            @Override
            public void run()
            {
                GarageDoorActionCallbackRegistry
                        .unregisterActionCallback(GarageDoorBotController.this);

                ExponentialBackoffService backoffService = new ExponentialBackoffService();
                while (backoffService.shouldRetry())
                {
                    try
                    {
                        GarageDoorBotController.this
                                .setName(GarageDoorConfigHolder.garageDoorConfig
                                        .getIrcNickname());
                        startIdentServer();
                        connect(GarageDoorConfigHolder.garageDoorConfig
                                .getIrcServerAddress());
                        joinChannel(GarageDoorConfigHolder.garageDoorConfig
                                .getIrcChannel());
                        GarageDoorActionCallbackRegistry.registerActionCallback(
                                GarageDoorBotController.this);
                        backoffService.doNotRetry();
                    }
                    catch (Exception ex)
                    {
                        backoffService.errorOccured();
                        log.error("Could not connect to IRC server: ", ex);
                        if (!backoffService.shouldRetry())
                        {
                            backoffService.reset();
                        }
                    }
                }
            }
        });
    }

    /**
     * Performs a requested command
     */
    private void performCommand(String name, String channel, String sender)
    {
        ArrayList<OpenerObject> garageDoorOpenerDetails;
        try
        {
            garageDoorOpenerDetails = MyQApiController.getGarageDoorDetails();
        }
        catch (Exception ex)
        {
            sendMessage(channel, sender
                    + ": there was an error executing the command for " + name);
            return;
        }

        if (garageDoorOpenerDetails != null)
        {
            garageDoorOpenerDetails.stream().filter(o -> o.name.equals(name))
                    .forEach(opener -> {
                        exService.execute(new Runnable()
                        {
                            /** @inheritDoc */
                            @Override
                            public void run()
                            {
                                try
                                {
                                    serialNumberToChannel
                                            .put(opener.serialNumber, channel);
                                    serialNumberToSender
                                            .put(opener.serialNumber, sender);
                                    MyQApiController.switchOpenerState(
                                            opener.serialNumber);
                                }
                                catch (Exception e)
                                {
                                    sendMessage(channel, sender
                                            + ": there was an error executing the command for "
                                            + name);
                                }
                            }
                        });
                        return;
                    });
        }
    }

    /**
     * Called back when a message is received by the bot
     */
    @Override
    public void onMessage(String channel, String sender, String login,
            String hostname, String message)
    {
        List<String> commandList = Splitter.on(' ').splitToList(message);

        if (commandList.size() >= 2)
        {
            String name = commandList.get(0);
            String command = commandList.get(1);

            if (command.equals("toggle"))
            {
                performCommand(name, channel, sender);
                return;
            }
            else if (command.equals("status"))
            {
                sendStatus(MetadataUtils.getSerialNumberForName(name), channel,
                        sender);
                return;
            }
        }
        sendMessage(channel,
                sender + ": Command not understood. Valid commands:");
        sendMessage(channel,
                sender + ": <name> toggle (opens or closes garage)");
        sendMessage(channel,
                sender + ": <name> status (gives current status of garage)");
        return;
    }

    /**
     * Sends the current status
     */
    private void sendStatus(String serialNumber, String channel, String sender)
    {
        try
        {
            ArrayList<OpenerObject> openers = MyQApiController
                    .getGarageDoorDetails();

            AtomicInteger count = new AtomicInteger(0);

            openers.stream().filter(o -> o.serialNumber.equals(serialNumber))
                    .forEach(o -> {
                        count.incrementAndGet();
                        sendMessage(channel, sender + ": current status for "
                                + o.name + " is " + o.state.getValue());
                    });

            // If the count is greater than zero, we can just return, no need
            // for further processing.
            if (count.get() > 0)
            {
                return;
            }
        }
        catch (Exception e)
        {
            // Do nothing
        }

        sendMessage(channel, sender
                + ": current status is not available for opener with serial number "
                + serialNumber);
    }

    /**
     * Schedule periodic updates of the status of the garage door opener
     */
    private void schedulePeriodicUpdate(String serialNumber)
    {
        Timer statusUpdateTimer = new Timer();
        final String channel = serialNumberToChannel.remove(serialNumber);
        final String sender = serialNumberToSender.remove(serialNumber);

        TimerTask statusUpdateTimerTask = new TimerTask()
        {
            int count = 0;

            /** @inheritDoc */
            @Override
            public void run()
            {
                log.debug("Status Update Timer Fired");

                sendStatus(serialNumber,
                        channel == null
                                ? GarageDoorConfigHolder.garageDoorConfig
                                        .getIrcChannel()
                                : channel,
                        sender == null
                                ? GarageDoorConfigHolder.garageDoorConfig
                                        .getIrcDefaultSenderNickname()
                                : sender);
                count++;
                if (count == (GarageDoorConstants.STATUS_UPDATE_INTERVALS_TO_EXECUTE
                        - 1))
                {
                    cancel();
                }
            }
        };
        statusUpdateTimer.scheduleAtFixedRate(statusUpdateTimerTask,
                GarageDoorConstants.STATUS_UPDATE_INTERVALS_TIME_PERIOD_DELAY_MS,
                GarageDoorConstants.STATUS_UPDATE_INTERVALS_TIME_PERIOD_DELAY_MS);
    }

    /** @inheritDoc */
    @Override
    public void onGarageDoorActionChange(String serialNumber)
    {
        schedulePeriodicUpdate(serialNumber);
    }

    /** @inheritDoc */
    public void onDisconnect()
    {
        this.initialize();
    }

}