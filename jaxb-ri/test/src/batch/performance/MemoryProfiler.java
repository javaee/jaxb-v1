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
