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
