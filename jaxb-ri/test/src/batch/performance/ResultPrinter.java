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
