/*
 * @(#)$Id: ResultPrinter.java,v 1.1 2004-06-25 21:13:03 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.performance;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;

/**
 * Listens to the performance measurement result.
 * <p>
 * You still need a normal {@link TestListener} to print
 * out how the test is going. This class just prints out the
 * performance measurement results.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ResultPrinter implements TestListener {

    public void addError(Test test, Throwable t) {
    }

    public void addFailure(Test test, AssertionFailedError t) {
    }

    public void endTest(Test test) {
        if(!(test instanceof PerformanceTest.ConfigTest))
            return;
        
        PerformanceTest.ConfigTest ctest = (PerformanceTest.ConfigTest)test;
        long result = ctest.getResult();
        
        System.out.println("performance result "+ctest.getName());
        System.out.println(result);
    }

    public void startTest(Test test) {
    }

}
