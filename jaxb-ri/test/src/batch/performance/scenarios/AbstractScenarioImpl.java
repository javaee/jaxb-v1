/*
 * @(#)$Id: AbstractScenarioImpl.java,v 1.1 2004-06-25 21:13:04 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.performance.scenarios;

import batch.core.om.Instance;
import batch.performance.PerformanceTestDescriptor;
import batch.performance.Scenario;
import batch.performance.ScenarioException;

/**
 * A helper {@link Scenario} implementation that keeps
 * all the parameters to the fields.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
abstract class AbstractScenarioImpl implements Scenario {
    protected PerformanceTestDescriptor descriptor;
    protected ClassLoader classLoader;
    protected Instance instance;
    
    public void prepare(PerformanceTestDescriptor descriptor, ClassLoader classLoader, Instance instance) throws ScenarioException {
        this.descriptor = descriptor;
        this.classLoader = classLoader;
        this.instance = instance;
    }

    public void teardown() throws ScenarioException {
        descriptor = null;
        classLoader = null;
        instance = null;
    }
}
