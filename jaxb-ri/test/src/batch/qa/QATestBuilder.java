/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package batch.qa;

import java.io.File;

import util.ConfigFailureTest;

import batch.core.JAXBTest;
import batch.core.TestBuilder;
import batch.core.compiler.Compiler;

/**
 * JUnit {@link Test} builder for ECMAScript-based test cases
 * (those tests that use testspec.meta files)
 * 
 * Call the build method to build tests.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class QATestBuilder implements TestBuilder {
    public JAXBTest createTest(File testSpecFile, Compiler compiler) {
        try {
            return createTest( new QATestDescriptor(testSpecFile), compiler );
        } catch( Exception e ) {
            return new ConfigFailureTest(testSpecFile,e);
        }
    }

    /**
     * 
     * <p>
     * Chance for a derived class to change the test case class.
     */
    protected JAXBTest createTest(QATestDescriptor descriptor, Compiler compiler) {
        return new QATestCase(descriptor,compiler);
    }
}
