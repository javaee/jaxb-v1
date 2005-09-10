package com.sun.timer;
/*
 * @(#)$Id: Timer.java,v 1.2 2005-09-10 18:19:41 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
