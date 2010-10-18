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

package batch.performance;

import batch.core.om.Schema;
import batch.performance.PerformanceTest.ConfigTest;
import batch.performance.PerformanceTestDescriptor.Config;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;

/**
 * {@link TestListener} that can listen to the performance test result.
 * 
 * <p>
 * The callback invocations follow the following BNF:
 * <pre>
 * ALL = GROUP*
 * GROUP = startGroup COMPILER TEST* endGroup
 * COMPILER = startCompiler endCompiler
 * TEST = startTest endTest
 * </pre>
 * 
 * <p>
 * This listener won't be notificed for other JUnit tests such as a
 * compilation of a schema.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class PerformanceTestListener implements TestListener {
    //  FIXME: things like startGroup/endGroup make sense
    // in the batch.core level.


//
//
//  callbacks
//
//
    
    /**
     * Called when a test case (that corresponds to
     * one <code>testspec.meta</code> file) starts.  
     */
    public abstract void startGroup( PerformanceTestDescriptor descriptor );

    /**
     * Called when a test case (that corresponds to
     * one <code>testspec.meta</code> file) ends.  
     */
    public abstract void endGroup( PerformanceTestDescriptor descriptor );
    
    /**
     * Called when a compilation starts.
     */
    public abstract void startCompiler( Schema schema );
    
    /**
     * Called when a compilation ends.
     * 
     * @param nanoSeconds
     *      turn around time of the compiler in nano-seconds.
     */
    public abstract void endCompiler( Schema schema, long nanoSeconds );
    
    /**
     * Called when a test starts.
     */
    public abstract void startTest(Config config);
    
    /**
     * Called when a test ends.
     * 
     * @param result
     *      The measurement result. -1 if it failed.
     *      If it's a speed test, the value is
     *      the turn around time in nano-seconds.
     *      If it's a foot print test, the value is
     *      the consumed memory is in bytes.
     */
    public abstract void endTest  (Config config,long result);




//
//
// event conversion logic
//
//
    public final void startTest(Test test) {
        if(test instanceof PerformanceTest) {
            PerformanceTest ptest = (PerformanceTest)test;
            startGroup(ptest.descriptor);
        }
        if(test instanceof PerformanceTest.CompileTest) {
            PerformanceTest.CompileTest ctest = (PerformanceTest.CompileTest)test;
            startCompiler(ctest.schema);
        }
        if(test instanceof PerformanceTest.ConfigTest) {
            ConfigTest ctest = (PerformanceTest.ConfigTest)test;
            startTest( ctest.config );
        }
    }


    public final void endTest(Test test) {
        if(test instanceof PerformanceTest) {
            PerformanceTest ptest = (PerformanceTest)test;
            endGroup(ptest.descriptor);
        }
        if(test instanceof PerformanceTest.CompileTest) {
            PerformanceTest.CompileTest ctest = (PerformanceTest.CompileTest)test;
            endCompiler(ctest.schema,ctest.getTurnAroundTime());
        }
        if(test instanceof PerformanceTest.ConfigTest) {
            PerformanceTest.ConfigTest ctest = (PerformanceTest.ConfigTest)test;
            endTest( ctest.config, ctest.getResult() );
        }
    }

    public void addError(Test test, Throwable t) {
    }

    public void addFailure(Test test, AssertionFailedError t) {
    }
}
