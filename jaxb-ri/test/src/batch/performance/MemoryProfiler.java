/*
 * @(#)$Id: MemoryProfiler.java,v 1.1 2004-06-25 21:13:02 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.performance;

/**
 * {@link Profiler} for memory foot print.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class MemoryProfiler implements Profiler {
    /**
     * Use {@link Profiler#MEMORY} singleton instance.
     */
    MemoryProfiler() {}
    
    public String name() { 
        return "memory";
    }
    
    public long run(Scenario scenario) throws ScenarioException {
                
        gc();
        
        long use = getMemoryUsage();
        
        scenario.run();
            
        gc();
        
        return getMemoryUsage()-use;
    }

    private long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Do the full GC.
     */
    private void gc() throws Error {
        try {
            Runtime.getRuntime().gc();
            Thread.sleep(5000); // encourage the GC thread to run.
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

}
