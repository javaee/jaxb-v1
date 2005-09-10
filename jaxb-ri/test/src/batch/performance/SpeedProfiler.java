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
