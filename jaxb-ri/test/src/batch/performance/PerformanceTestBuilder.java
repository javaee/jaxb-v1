/*
 * @(#)$Id: PerformanceTestBuilder.java,v 1.1 2004-06-25 21:13:02 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.performance;

import java.io.File;

import util.*;

import batch.core.JAXBTest;
import batch.core.TestBuilder;
import batch.core.compiler.Compiler;

/**
 * Test builder that builds {@link PerformanceTest}s.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class PerformanceTestBuilder implements TestBuilder {
    public JAXBTest createTest(File testSpecFile, Compiler compiler ) {
        try {
            PerformanceTestDescriptor descriptor = loadDescriptor(testSpecFile);
            if( descriptor.configs.length==0 )
                // no performance test for this.
                return NullJAXBTest.theInstance;
            return createTest(descriptor, compiler);
        } catch( Exception e ) {
            // other config failure
            return new ConfigFailureTest(testSpecFile,e);
        }
    }
    
    /**
     * Parses the given file into a descriptor.
     * 
     * <p>
     * Chance for a derived class to change the test case class.
     */
    protected PerformanceTestDescriptor loadDescriptor(File testSpecFile) throws Exception {
        return new PerformanceTestDescriptor(testSpecFile);
    }

    /**
     * 
     * <p>
     * Chance for a derived class to change the test case class.
     */
    protected PerformanceTest createTest(PerformanceTestDescriptor descriptor, Compiler compiler) {
        return new PerformanceTest(descriptor,compiler);
    }
}
