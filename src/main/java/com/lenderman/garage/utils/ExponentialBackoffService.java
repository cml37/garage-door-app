/*******************************************************
 * Exponential Backoff Service
 *******************************************************/
package com.lenderman.garage.utils;

import java.util.Random;

/**
 * Service utility class to allow for exponential backoff
 *
 * @author Chris Lenderman
 */
public class ExponentialBackoffService
{
    /** Random number generator */
    private final Random random = new Random();

    /** Default number of retries with exponential backoff */
    public static int DEFAULT_RETRIES = 50;

    /** Default wait time in milliseconds for wait retry */
    public static long DEFAULT_WAIT_TIME_MILLIS = 10000;

    /** Number of remaining retries */
    private int numberOfTriesLeft;

    /** The remaining time to wait */
    private long timeToWait;

    /**
     * Constructor
     */
    public ExponentialBackoffService()
    {
        reset();
    }

    /**
     * Whether or not a retry should be attempted
     */
    public boolean shouldRetry()
    {
        return numberOfTriesLeft > 0;
    }

    /**
     * Process the occurrence of an error
     */
    public void errorOccured()
    {
        numberOfTriesLeft--;
        waitUntilNextTry();
        timeToWait += random.nextInt(1000);
    }

    /**
     * Execute the actual wait
     */
    private void waitUntilNextTry()
    {
        try
        {
            Thread.sleep(timeToWait);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * When success is achieved, this is called to clear out any additional
     * attempts
     */
    public void doNotRetry()
    {
        numberOfTriesLeft = 0;
    }

    /**
     * Reset the backoff attempts
     */
    public void reset()
    {
        this.numberOfTriesLeft = DEFAULT_RETRIES;
        this.timeToWait = DEFAULT_WAIT_TIME_MILLIS;
    }
}