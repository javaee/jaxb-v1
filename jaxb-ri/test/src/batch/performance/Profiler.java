/*
 * @(#)$Id: Profiler.java,v 1.1 2004-06-25 21:13:03 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.performance;

/**
 * Object that is responsible for measuring the performance
 * (such as foot print or speed) of a {@link Scenario}.
 * 
 * <p>
 * {@link Profiler}s are state-less.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface Profiler {
    
    /**
     * Gets the human-readable name of this Profiler.
     */
    String name();
    
    /**
     * Calls the {@link Scenario#run()} methods and measures
     * the performance.
     * 
     * <p>
     * The scenario will be set up and torn down by the caller.
     * 
     * @param scenario
     *      prepared scenario.
     * 
     * @throws ScenarioException
     *      If the specified {@link Scenario} object fails to
     *      run.
     * 
     * @return
     *      return the measured performance number to be recorded.
     */
    long run( Scenario scenario ) throws ScenarioException;
    
    public static final Profiler MEMORY = new MemoryProfiler();
    public static final Profiler SPEED  = new SpeedProfiler();
}
