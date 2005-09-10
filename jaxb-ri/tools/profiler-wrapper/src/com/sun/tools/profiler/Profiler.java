/*
 * @(#)$Id: Profiler.java,v 1.2 2005-09-10 18:19:46 kohsuke Exp $
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
