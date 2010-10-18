/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
