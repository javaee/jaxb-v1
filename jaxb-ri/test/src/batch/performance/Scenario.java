/*
 * @(#)$Id: Scenario.java,v 1.1 2004-06-25 21:13:03 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.performance;

import batch.core.om.Instance;

/**
 * Scenario to be measured. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface Scenario {
    
    /**
     * Human readable name of the scenario.
     */
    public String getName();
    
    /**
     * Called first to prepare the scenario.
     * 
     * The code that runs in this method is not considered
     * as a subject of the measurement.
     * 
     * @param classLoader
     *      This {@link ClasLoader} can load the generated classes.
     *      Use the reflection to create the ObjectFactory class
     *      in the root package.
     * @param instance
     *      If the scenario is associated with an instance, that instance.
     *      Otherwise null to indicate that the scenario is run once
     *      against the whole {@link PerformanceTestDescriptor}.
     */
    void prepare( PerformanceTestDescriptor descriptor, ClassLoader classLoader, Instance instance ) throws ScenarioException;    
    
    // TODO: instead of passing a ClassLoader, we might want to pass in
    // something more, so that the method can easily access JAXBContext,
    // for instance. 
    
    /**
     * Called to execute the scenario after the {@link #prepare()} method.
     * 
     * This method will become the subject of measurement.
     * Those scenarios whose memory footprint is measured should
     * use class fields to store references so that the objects
     * will stay in memory even after this method returns.
     */
    void run() throws ScenarioException;
    
    /**
     * Called after the {@link #run()} method to release unnecessary resources.
     * 
     * The code that runs in this method is not considered
     * as a subject of the measurement.
     */
    void teardown() throws ScenarioException;
}
