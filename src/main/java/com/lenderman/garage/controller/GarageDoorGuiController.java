/*******************************************************
 * Garage Door GUI Controller
 *******************************************************/
package com.lenderman.garage.controller;

import java.awt.Container;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.lenderman.garage.GarageDoorConstants;
import com.lenderman.garage.api.MyQApiController;
import com.lenderman.garage.callback.GarageDoorActionCallback;
import com.lenderman.garage.callback.GarageDoorActionCallbackRegistry;
import com.lenderman.garage.jfd.GarageDoorGuiFrame;
import com.lenderman.garage.jfd.GarageDoorInstance;
import com.lenderman.garage.types.OpenerObject;
import com.lenderman.garage.utils.DurationUtils;
import com.lenderman.garage.utils.NotificationUtils;
import com.lenderman.garage.utils.OsUtils;
import com.lenderman.garage.utils.SystemTrayWrapper;
import com.lenderman.garage.utils.TrayIconWrapper;

/**
 * Main GUI controller for the application
 *
 * @author Chris Lenderman
 */
@SuppressWarnings("serial")
// TODO need to look at threading model. We are doing API work on AWT thread
public class GarageDoorGuiController extends GarageDoorGuiFrame
        implements GarageDoorActionCallback
{
    /** Class logger */
    private static Logger log = Logger.getLogger(GarageDoorGuiController.class);

    /** Single threaded executor service used principally to make API calls */
    private final ExecutorService exService = Executors
            .newSingleThreadExecutor();

    /** Status update timer */
    private Timer statusUpdateTimer = new Timer();

    /** Status update timer task */
    private TimerTask statusUpdateTimerTask = null;

    /** Duration update timer task */
    private TimerTask durationUpdateTimerTask = null;

    /**
     * Button change listener class used to support changing garage door state
     */
    private class ButtonChangeListener implements ActionListener
    {
        String serialNumber = null;

        /**
         * Sets the Serial Number of the garage door for use with the button
         * listener
         *
         * @param String
         */
        public void setSerialNumber(String serialNumber)
        {
            this.serialNumber = serialNumber;
        }

        /** @inheritDoc */
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            exService.execute(new Runnable()
            {
                /** @inheritDoc */
                @Override
                public void run()
                {
                    switchOpenerState(serialNumber);
                }
            });
        }
    }

    /**
     * Holder class to tie together the GUI instances and corresponding button
     * change listener
     */
    private class GarageDoorInstanceAndListenerHolder
    {
        private GarageDoorInstance instance;
        private ButtonChangeListener buttonListener;
        private OpenerObject openerData;
    }

    /**
     * List of garage door instances active on the GUI, as well as corresponding
     * button listeners
     */
    private ArrayList<GarageDoorInstanceAndListenerHolder> garageDoorInstances = new ArrayList<GarageDoorInstanceAndListenerHolder>();

    /**
     * Constructor
     */
    public GarageDoorGuiController()
    {
        GarageDoorActionCallbackRegistry.registerActionCallback(this);
        initTrayIcon();

        if (OsUtils.isLinuxOS() || !SystemTrayWrapper.isSupported())
        {
            setVisible(true);
        }

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                updateGuiOpenerStates();
            }
        });

        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowActivated(WindowEvent arg0)
            {
                updateGuiOpenerStates();

                if (durationUpdateTimerTask != null)
                {
                    durationUpdateTimerTask.cancel();
                }

                durationUpdateTimerTask = new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                log.debug("Duration update time fired");
                                updateDuration();
                            }
                        });
                    }
                };

                statusUpdateTimer.scheduleAtFixedRate(durationUpdateTimerTask,
                        0, 5000);
            }

            @Override
            public void windowDeactivated(WindowEvent arg0)
            {
                if (durationUpdateTimerTask != null)
                {
                    durationUpdateTimerTask.cancel();
                }
            }
        });
    }

    /**
     * Updates opener states on GUI
     */
    private void updateGuiOpenerStates()
    {
        try
        {
            ArrayList<OpenerObject> openers = MyQApiController
                    .getGarageDoorDetails();

            if (openers.size() != garageDoorInstances.size())
            {
                log.debug(
                        "Change detected in number of actual openers vs. number of displayed openers");
                garageDoorInstances.clear();

                FormLayout fLayout = (FormLayout) getContentPane().getLayout();
                while (fLayout.getRowCount() > 5)
                {
                    fLayout.removeRow(1);
                }

                for (int index = 0; index < openers.size(); index++)
                {
                    fLayout.insertRow(1, FormFactory.RELATED_GAP_ROWSPEC);
                    fLayout.insertRow(1, FormFactory.DEFAULT_ROWSPEC);
                    Container contentPane = getContentPane();

                    GarageDoorInstanceAndListenerHolder holder = new GarageDoorInstanceAndListenerHolder();
                    holder.instance = new GarageDoorInstance();
                    holder.buttonListener = new ButtonChangeListener();
                    holder.instance.buttonChange
                            .addActionListener(holder.buttonListener);
                    garageDoorInstances.add(holder);

                    contentPane.add(holder.instance, CC.xy(1, 1));
                }
                getContentPane().setLayout(fLayout);
            }

            String toolTip = new String();
            for (int index = 0; index < openers.size(); index++)
            {
                GarageDoorInstanceAndListenerHolder gdi = garageDoorInstances
                        .get(index);
                gdi.openerData = openers.get(index);

                gdi.instance.labelName.setText(gdi.openerData.name);
                gdi.instance.labelStatus
                        .setText(gdi.openerData.state.toString());
                gdi.buttonListener.setSerialNumber(gdi.openerData.serialNumber);
                toolTip += (index > 0 ? " " : "") + gdi.openerData.name + ":"
                        + gdi.openerData.state.toString();
            }
            this.labelLastUpdatedDate.setText(new Date().toString());
            NotificationUtils.setTrayIconTipText(toolTip);
            updateDuration();
        }
        catch (Exception ex)
        {
            log.error("Couldn't get opener states: " + ex);
        }
    }

    /**
     * Updates the GUI duration labels indicating how long it has been since the
     * last garage door state change
     */
    private void updateDuration()
    {
        for (GarageDoorInstanceAndListenerHolder gdi : garageDoorInstances)
        {
            Duration duration = new Interval(gdi.openerData.stateUpdatedTime,
                    new DateTime().getMillis()).toDuration();

            gdi.instance.labelDuration.setText(DurationUtils
                    .getDurationPrettyPrint(duration.getStandardMinutes()));
        }
    }

    /**
     * Schedules a periodic GUI update while the garage door status is changing
     */
    private void scheduleGuiPeriodicUpdate()
    {
        if (statusUpdateTimerTask != null)
        {
            statusUpdateTimerTask.cancel();
        }
        statusUpdateTimerTask = new TimerTask()
        {
            int count = 0;

            /** @inheritDoc */
            @Override
            public void run()
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    /** @inheritDoc */
                    @Override
                    public void run()
                    {
                        log.debug("Status Update Timer Fired");
                        updateGuiOpenerStates();
                        count++;
                        if (count == (GarageDoorConstants.STATUS_UPDATE_INTERVALS_TO_EXECUTE
                                - 1))
                        {
                            cancel();
                        }
                    }
                });
            }
        };
        statusUpdateTimer.scheduleAtFixedRate(statusUpdateTimerTask,
                GarageDoorConstants.STATUS_UPDATE_INTERVALS_TIME_PERIOD_DELAY_MS,
                GarageDoorConstants.STATUS_UPDATE_INTERVALS_TIME_PERIOD_DELAY_MS);
    }

    /**
     * Switches the opener state for the given Serial Number
     */
    private void switchOpenerState(String serialNumber)
    {
        try
        {
            MyQApiController.switchOpenerState(serialNumber);
        }
        catch (Exception ex)
        {
            log.error("Couldn't switch opener states: " + ex);
        }
    }

    /**
     * Initializes the tray icon
     */
    private void initTrayIcon()
    {
        // Check to make sure that the tray icon is supported.
        if (SystemTrayWrapper.isSupported()
                && TrayIconWrapper.trayIconSupported())
        {
            PopupMenu popup = new PopupMenu();
            popup.add(new MenuItem("Show Controller..."));
            popup.addSeparator();
            popup.add(new MenuItem("Exit"));

            NotificationUtils.setTrayIconPopupMenu(popup);
            NotificationUtils.addTrayIconMouseListener(new MouseAdapter()
            {
                /** @inheritDoc */
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    if ((e.getClickCount() == 2)
                            && (e.getButton() == MouseEvent.BUTTON1))
                    {
                        GarageDoorGuiController.this.setVisible(true);
                    }
                }
            });

            popup.addActionListener(new ActionListener()
            {
                /** @inheritDoc */
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    if (evt.getActionCommand().equals("Show Controller..."))
                    {
                        setVisible(true);
                    }
                    else if (evt.getActionCommand().equals("Exit"))
                    {
                        System.exit(0);
                    }
                }
            });

            buttonUpdateStatus.addActionListener(new ActionListener()
            {
                /** @inheritDoc */
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    exService.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            SwingUtilities.invokeLater(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    updateGuiOpenerStates();
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    /** @inheritDoc */
    @Override
    public void onGarageDoorActionChange(String serialNumber)
    {
        scheduleGuiPeriodicUpdate();
    }
}