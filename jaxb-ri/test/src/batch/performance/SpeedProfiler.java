/*
 * @(#)$Id: SpeedProfiler.java,v 1.1 2004-06-25 21:13:04 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.performance;

import com.sun.timer.Timer;

/**
 * {@link Profiler} that measures the speed of a scenario execution.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class SpeedProfiler implements Profiler {
    /**
     * Use {@link Profiler#SPEED} singleton instance.
     */
    SpeedProfiler() {}
    
    /**
     * Timer library
     */
    private static final Timer timer;

    /**
     * Controlls external profilers.
     */
    private static final com.sun.tools.profiler.Profiler extProfiler = com.sun.tools.profiler.Profiler.create();
    
    static {
        timer = Timer.theInstance;
        System.out.println("Using "+timer.name()+" timer");
    }
    
    public String name() { 
        return "speed";
    }
    
    /**
     * Returns the milli-seconds it took to execute the run.
     */
    public long run(Scenario scenario) throws ScenarioException {
        // do the full GC now to avoid unnecessary GCs
        // during the run.
        gc();
        
        extProfiler.enable();
            
        long start = timer.nanoTime();
        
        scenario.run();
            
        long number = timer.nanoTime()-start;

        extProfiler.disable();
        
        
        return number;
    }

    private void gc() throws Error {
        try {
            Runtime.getRuntime().gc();
            Thread.sleep(5000); // encourage the GC thread to run.
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }
}
