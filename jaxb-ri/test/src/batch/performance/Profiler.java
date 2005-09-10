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
