package com.sun.timer;
/*
 * @(#)$Id: Timer.java,v 1.1 2004-06-25 21:13:38 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 * High-precision timer.
 * 
 * <p>
 * This class allows applications to safely take advantage of
 * various high-precision timers available in various versions
 * of JDKs, etc.
 * 
 * <p>
 * Whether the timer is actually high in precision depends on
 * whether such a library was found or not.  
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class Timer {
    Timer() {}
    
    /**
     * The singleton {@link Timer} instance.
     */
    public static final Timer theInstance;
    
    /**
     * Returns the current value of the system timer, in nanoseconds.
     * 
     * <p>
     * This method provides nanosecond precision, but not necessarily
     * nanosecond accuracy. No guarantees are made about how
     * frequently values change.
     */
    public abstract long nanoTime(); 
    
    /**
     * Human readable name of the timer.
     */
    public abstract String name();
    
    static {
        Timer i = null;
        try {
            i = new HighResTimer();
        } catch( Throwable t ) {
            i= new LowLesTimer();
        }
        theInstance = i;
    }
    
    
}
