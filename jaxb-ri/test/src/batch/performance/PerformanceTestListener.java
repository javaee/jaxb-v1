/*
 * @(#)$Id: PerformanceTestListener.java,v 1.1 2004-06-25 21:13:03 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.performance;

import batch.core.om.Schema;
import batch.performance.PerformanceTest.ConfigTest;
import batch.performance.PerformanceTestDescriptor.Config;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;

/**
 * {@link TestListener} that can listen to the performance test result.
 * 
 * <p>
 * The callback invocations follow the following BNF:
 * <pre>
 * ALL = GROUP*
 * GROUP = startGroup COMPILER TEST* endGroup
 * COMPILER = startCompiler endCompiler
 * TEST = startTest endTest
 * </pre>
 * 
 * <p>
 * This listener won't be notificed for other JUnit tests such as a
 * compilation of a schema.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class PerformanceTestListener implements TestListener {
    //  FIXME: things like startGroup/endGroup make sense
    // in the batch.core level.


//
//
//  callbacks
//
//
    
    /**
     * Called when a test case (that corresponds to
     * one <code>testspec.meta</code> file) starts.  
     */
    public abstract void startGroup( PerformanceTestDescriptor descriptor );

    /**
     * Called when a test case (that corresponds to
     * one <code>testspec.meta</code> file) ends.  
     */
    public abstract void endGroup( PerformanceTestDescriptor descriptor );
    
    /**
     * Called when a compilation starts.
     */
    public abstract void startCompiler( Schema schema );
    
    /**
     * Called when a compilation ends.
     * 
     * @param nanoSeconds
     *      turn around time of the compiler in nano-seconds.
     */
    public abstract void endCompiler( Schema schema, long nanoSeconds );
    
    /**
     * Called when a test starts.
     */
    public abstract void startTest(Config config);
    
    /**
     * Called when a test ends.
     * 
     * @param result
     *      The measurement result. -1 if it failed.
     *      If it's a speed test, the value is
     *      the turn around time in nano-seconds.
     *      If it's a foot print test, the value is
     *      the consumed memory is in bytes.
     */
    public abstract void endTest  (Config config,long result);




//
//
// event conversion logic
//
//
    public final void startTest(Test test) {
        if(test instanceof PerformanceTest) {
            PerformanceTest ptest = (PerformanceTest)test;
            startGroup(ptest.descriptor);
        }
        if(test instanceof PerformanceTest.CompileTest) {
            PerformanceTest.CompileTest ctest = (PerformanceTest.CompileTest)test;
            startCompiler(ctest.schema);
        }
        if(test instanceof PerformanceTest.ConfigTest) {
            ConfigTest ctest = (PerformanceTest.ConfigTest)test;
            startTest( ctest.config );
        }
    }


    public final void endTest(Test test) {
        if(test instanceof PerformanceTest) {
            PerformanceTest ptest = (PerformanceTest)test;
            endGroup(ptest.descriptor);
        }
        if(test instanceof PerformanceTest.CompileTest) {
            PerformanceTest.CompileTest ctest = (PerformanceTest.CompileTest)test;
            endCompiler(ctest.schema,ctest.getTurnAroundTime());
        }
        if(test instanceof PerformanceTest.ConfigTest) {
            PerformanceTest.ConfigTest ctest = (PerformanceTest.ConfigTest)test;
            endTest( ctest.config, ctest.getResult() );
        }
    }

    public void addError(Test test, Throwable t) {
    }

    public void addFailure(Test test, AssertionFailedError t) {
    }
}
