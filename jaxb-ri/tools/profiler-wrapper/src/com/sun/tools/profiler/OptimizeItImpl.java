/*
 * @(#)$Id: OptimizeItImpl.java,v 1.1 2004-06-25 21:13:49 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.profiler;

import intuitive.audit.Audit;

/**
 * {@link Profiler} implementation for OptimizeIt.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class OptimizeItImpl extends Profiler {
    OptimizeItImpl() {
       // check if the Audit API class is available
       Object o = Audit.class;
    }
    public void enable() {
        Audit.enableCPUProfiler();
    }
    public void disable() {
        Audit.disableCPUProfiler();
    }
}
