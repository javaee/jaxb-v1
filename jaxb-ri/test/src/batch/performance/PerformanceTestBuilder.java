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
