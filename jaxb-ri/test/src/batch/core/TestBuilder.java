/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package batch.core;

import java.io.File;

import batch.core.compiler.Compiler;

/**
 * abstract JUnit {@link Test} builder 
 * (those tests that use testspec.meta files)
 * 
 * Call the build method to build tests.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface TestBuilder {
    /**
     * Builds a test case from the specified description file.
     * 
     * @param compiler
     *      JAXB compiler engine to be used for schema compilation.
     * 
     * @return  non-null valid test object.
     */
    JAXBTest createTest( File testSpecFile, Compiler compiler );
}
