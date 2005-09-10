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
