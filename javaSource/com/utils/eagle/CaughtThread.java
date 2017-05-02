//=== File Prolog ============================================================
//
//	This code was developed by NASA, Goddard Space Flight Center, Code 685
//
//--- Warning ----------------------------------------------------------------
//	This software is property of the National Aeronautics and Space
//	Administration. Unauthorized use or duplication of this software is
//	strictly prohibited. Authorized users are subject to the following
//	restrictions:
//	*	Neither the author, their corporation, nor NASA is responsible for
//		any consequence of the use of this software.
//	*	The origin of this software must not be misrepresented either by
//		explicit claim or by omission.
//	*	Altered versions of this software must be plainly marked as such.
//	*	This notice may not be removed or altered.
//
//=== End File Prolog ========================================================

package com.utils.eagle;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A wrapper that automatically catches exceptions and reports when a thread
 * exits. Mainly helps with debugging.
 * <P>
 * This code was developed for NASA, Goddard Space Flight Center, Code 685
 * 
 * @version $Id: CaughtThread.java,v 1.1 2012/01/23 00:05:17 smaher Exp $
 * @author Steve Maher
 */
public class CaughtThread extends Thread
{
    /**
     * Logger for this class
     */
    private static final Logger sLogger = Logger.getLogger(CaughtThread.class.getName());

    private final Runnable fInnerRunnable;
    private final String fThreadName;

    public CaughtThread(String threadName, Runnable r)
    {
        fInnerRunnable = r;
        fThreadName = threadName;
    }

    @Override
    public void run()
    {
        sLogger.logp(Level.INFO, "CaughtThread:" + fThreadName, "run()", "start");
        Thread.currentThread().setName(fThreadName);
        try
        {
            fInnerRunnable.run();
        } catch (Throwable t)
        {
            sLogger.logp(Level.SEVERE, "CaughtThread:" + fThreadName, "run()", "Caught Throwable", t);
        } finally
        {
            sLogger.logp(Level.INFO, "CaughtThread:" + fThreadName, "run()", "Thread exitting.");
        }
    }
}
