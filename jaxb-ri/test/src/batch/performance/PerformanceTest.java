/*
 * @(#)$Id: PerformanceTest.java,v 1.1 2004-06-25 21:13:02 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.performance;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import batch.core.CompileTestCase;
import batch.core.JAXBTest;
import batch.core.JavacException;
import batch.core.XJCException;
import batch.core.compiler.Compiler;
import batch.core.om.Schema;
import batch.performance.PerformanceTestDescriptor.Config;

import com.sun.timer.Timer;
import com.sun.tools.xjc.reader.xmlschema.parser.VersionNumber;

/**
 * A {@link JAXBTest} that measures the performance (such as
 * time it takes to do a certain operation or the memory footprint.)
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class PerformanceTest extends TestSuite implements JAXBTest {
    
    final PerformanceTestDescriptor descriptor;
    
    private final CompileTest compileTest;
    
    public PerformanceTest(PerformanceTestDescriptor descriptor, Compiler compiler) {
        super(descriptor.getName());
        this.descriptor = descriptor;

        // first run the compiler
        compileTest = new CompileTest(descriptor.schema,compiler);
        addTest(compileTest);
        
        // then run each configuration
        for( int i=0; i<descriptor.configs.length; i++ )
            addTest( new ConfigTest(descriptor.configs[i]) );
    }

    public boolean isApplicable(VersionNumber v) {
        return descriptor.isApplicable(v);
    }

    /**
     * Configures the test so that the compilation of the schema
     * will be skipped.
     */
    public void skipCompiler() {
        compileTest.forceCompile = false;
    }
    
    public void run(TestResult result) {
        // overrided to report the start/end of this suite.
        // this could be potentially dangerous, as TestListner interface
        // doesn't necessarily suppor nested tests, but it seems to be 
        // working so far. 
        result.startTest(this);
        super.run(result);
        result.endTest(this);
    }

    public int countTestCases() {
        // since we are adding one more startTest/endTest callback,
        // add 1 to make ends meet.
        return super.countTestCases()+1;
    }

    
    private static final Timer timer = Timer.theInstance;
    
    
    /**
     * {@link CompileTestCase} with the performance measurement.
     */
    protected final static class CompileTest extends CompileTestCase {
        public CompileTest(Schema _schema, final Compiler _compiler) {
            super(_schema, new CompilerWrapper(_compiler) );
        }
        
        /**
         * Returns the turn around time of the compiler in nano-seconds.
         */
        public long getTurnAroundTime() {
            return ((CompilerWrapper)super.compiler).turnAroundTime;
        }
        
        /**
         * Runs the real {@link Compiler} and measure the turn around time.
         */
        private static class CompilerWrapper implements Compiler {
            /**
             * How long did it take to compile it?
             */
            private long turnAroundTime;
        
            /**
             * The real compiler.
             */
            private final Compiler _compiler;
            
            private CompilerWrapper(Compiler _compiler) {
                this._compiler = _compiler;
            }
            
            public ClassLoader compile(Schema schema) throws XJCException, JavacException {
                long start = timer.nanoTime();
                ClassLoader result = _compiler.compile(schema);
                turnAroundTime = timer.nanoTime()-start;
                return result;
            }
        }
    }
    
    protected class ConfigTest extends TestCase {
        
        protected final Config config;
        
        /**
         * Once the test is run, we recod the result here
         * so that {@link junit.framework.TestListener} can
         * query this value.
         */
        private long result = -1;

        ConfigTest( Config _config ) {
            super( _config.toString() );
            this.config = _config;
        }

        
        /**
         * Obtains the performance number measured.
         * 
         * @return -1
         *      If the measurement fails.
         */
        protected long getResult() {
            return result;
        }
        
        protected void runTest() throws ScenarioException {
            if(compileTest.getLoader()==null)
                fail("schema didn't compile");
            
            config.scenario.prepare(descriptor,compileTest.getLoader(),config.instance);
            result = config.profiler.run(config.scenario);
            config.scenario.teardown();
        }
    }
}
