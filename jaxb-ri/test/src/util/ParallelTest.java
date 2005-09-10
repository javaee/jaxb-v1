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
package util;

import java.util.Enumeration;
import java.util.Stack;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * Runs tests parallely by using multiple threads.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ParallelTest extends TestSuite {
    
    private final int numThreads;
    
    public ParallelTest( int numThreads ) {
        this.numThreads = numThreads;
    }
    
    /**
     * Runs the tests and collects their result in a TestResult.
     * 
     * <p>
     * {@link TestResult} seems to be designed with synchronization
     * in mind. So we don't do anything particular here.
     */
    public void run(final TestResult result) {
        final Stack s = new Stack();
        for (Enumeration e= tests(); e.hasMoreElements(); )
            s.add(e.nextElement());
        
        final Object stackLock = new Object();
        
        Thread[] threads = new Thread[numThreads];
        
        for( int i=0; i<numThreads; i++ ) {
            threads[i] = new Thread() {
                public void run() {
                    while(true) {
                        if (result.shouldStop()) return;
                        
                        Test test;
                        synchronized(stackLock) {
                            if(s.empty())   return;
                            test = (Test)s.pop();
                        }
                        runTest(test, result);
                    }
                }
            };
            
            threads[i].start();
        }
        
        try {
            for( int i=0; i<numThreads; i++ )
                threads[i].join();
        } catch( InterruptedException e ) {
            e.printStackTrace(); // FIXME: when this can happen?
        }
    }
    
    static class SynchronizedTestResult extends TestResult {
        public synchronized void endTest(Test test) {
            super.endTest(test);
        }

        protected synchronized void run(TestCase test) {
            super.run(test);
        }

        public synchronized void startTest(Test test) {
            super.startTest(test);
        }
    }
}
