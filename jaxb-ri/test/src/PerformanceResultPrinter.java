/*
 * @(#)$Id: PerformanceResultPrinter.java,v 1.1 2004-06-25 21:12:57 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
import batch.core.om.Schema;
import batch.performance.PerformanceTestListener;
import batch.performance.PerformanceTestDescriptor;
import batch.performance.PerformanceTestDescriptor.Config;

/**
 * Prints out the performance result to console.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class PerformanceResultPrinter extends PerformanceTestListener {

    public void startTest(Config config) {
    }

    public void endTest(Config config, long result) {
        System.out.println(result+"ns for "+config.getName());
    }

    public void startGroup(PerformanceTestDescriptor descriptor) {
    }

    public void endGroup(PerformanceTestDescriptor descriptor) {
    }

    public void startCompiler(Schema schema) {
    }

    public void endCompiler(Schema schema, long nanoSeconds) {
        System.out.println(nanoSeconds+"ns for compiling "+schema.schema.toExternalForm());
    }

}
