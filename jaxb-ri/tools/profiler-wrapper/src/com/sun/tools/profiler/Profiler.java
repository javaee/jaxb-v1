/*
 * @(#)$Id: Profiler.java,v 1.1 2004-06-25 21:13:49 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.profiler;

/**
 * External profiler API wrapper.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class Profiler {
    Profiler() {}
    
    /**
     * Creates a new {@link Profiler} instance.
     */
    public static Profiler create() {
        try {
            return new OptimizeItImpl();
        } catch( Throwable t ) {
            return noop;
        }
    }
    
    /**
     * Starts the CPU profiling.
     */
    public abstract void enable();
    
    /**
     * Stops the CPU profiling.
     */
    public abstract void disable();
    
    private static final Profiler noop = new Profiler() {
        public void enable() {}
        public void disable() {}
    };
}
