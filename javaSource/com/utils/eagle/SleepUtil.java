package com.utils.eagle;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public final class SleepUtil
{
    /**
     * Logger for this class
     */
    private static final Logger sLogger = Logger.getLogger(SleepUtil.class.getName());

    public static void sleepSeconds(double seconds)
    {
        sleepMillis((long) (seconds * 1000L), "<none>");
    }
    
    public static void sleepSeconds(double seconds, String context)
    {
        sleepMillis((long) (seconds * 1000L), context);
    }
    
    public static void sleepSecondsCountdown(int seconds, String context, JLabel lbl)
    {
        for (; seconds >= 0; seconds--)
        {
            final int remaining = seconds;
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    lbl.setText("" + remaining);
                }
            });
            if (seconds > 0)
            {
                sleepMillis(1000L, context);
            }    
        }
        
    }
    
    public static void sleepMillis(long millis, String context)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            sLogger.logp(Level.WARNING, "SleepUtil", "sleepMillis(long, String) - " + context, "Sleep interrupted");

        }
    }

    public static void sleepMillis(long millis)
    {
        sleepMillis(millis, "<no context");
        
    }    
    
    
}
