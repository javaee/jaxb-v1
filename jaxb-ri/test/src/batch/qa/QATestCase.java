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

import java.io.Reader;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import util.InterpreterBuilder;
import batch.core.CompileTestCase;
import batch.core.JAXBTest;
import batch.core.compiler.Compiler;
import batch.core.om.Instance;
import bsh.Interpreter;
import bsh.TargetError;

import com.sun.tools.xjc.reader.xmlschema.parser.VersionNumber;


/**
 * TestCase that handles one schema and XML documents associated
 * with them.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class QATestCase extends TestSuite implements JAXBTest {

    /** set to false to avoid unnecessary schema compilation. */
    public boolean forceCompile = true;
    
    protected final QATestDescriptor desc;
        
    /**
     * Builds a test case.
     */
    public QATestCase( QATestDescriptor description, Compiler compiler ) {
        desc = description;
        
        // first run the compiler
        compileTest = new CompileTestCase(desc.schema,compiler);
        addTest(compileTest);
        
        // then run sciprts with "run='once'" attribute.
        for( int i=0; i<desc.runOnceScripts.length; i++ ) {
            addTest(new ScriptTest(null,desc.runOnceScripts[i]));
        }
        
        // finally run per-instance scripts.
        for( int i=0; i<desc.instances.length; i++ ) {
            for( int j=0; j<desc.perInstanceScripts.length; j++ ) {
                addTest(new ScriptTest(desc.instances[i],desc.perInstanceScripts[j]));
            }
        }
    }
    
    protected final CompileTestCase compileTest;
    

    
    /**
     * Set up a script interpreter for "run once" tests. 
     * This method can be overrided by derived classes.
     */
    protected Interpreter createSchemaTestInterpreter() throws Exception {
        return InterpreterBuilder.create(
            compileTest.getLoader(),
            compileTest.schema.targetPackageName,
            true );
    }
    
    /**
     * Set up a script interpreter for "per-document" tests. 
     * This method can be overrided by derived classes.
     */
    protected Interpreter createInstanceTestInterpreter( Instance instance ) throws Exception {
        return InterpreterBuilder.create(
            instance,
            compileTest.getLoader(),
            compileTest.schema.targetPackageName );
    }    
    
    
    /**
     * {@link TestCase} that runs a script.
     */
    private class ScriptTest extends TestCase {
        private final Instance instance;
        private final Script script;
        
        ScriptTest( Instance instance, Script script ) {
            super(
                instance==null
                    ?"running: "+script.getName()
                    :"running: "+instance.getName()+" with "+script.getName());
            this.instance = instance;
            this.script = script;
        }
        
        
        public void runTest() throws Exception {
            System.out.println("running "+super.getName());
            
            if(compileTest.getLoader()==null)
                // skip the test.
                // TODO: it would be nice if JUnit supports the concept of
                // "unexecuted" tests.
                fail("schema didn't compile");
            
            Reader scriptReader = script.getScriptReader();
            
            if( instance==null ) {
                Interpreter interpreter = createSchemaTestInterpreter();
                try {
                    interpreter.eval(scriptReader);
                } catch( TargetError e ) {
                    Throwable t = e.getTarget();
                    if(t instanceof Exception)
                        throw (Exception)t;
                    else
                        throw (Error)t;
                }
            } else {
                instance.runScript(
                    createInstanceTestInterpreter(instance),
                    scriptReader );
            }
        }
        
        public String toString() {
            return getName();
        }
    }
    
    /**
     * Configures the test so that the compilation of the schema
     * will be skipped.
     */
    public void skipCompiler() {
        compileTest.forceCompile = false;
    }
    
    /**
     * This test is valid against the JAXB RI of this version or later.
     */
    public boolean isApplicable(VersionNumber v) {
        return desc.isApplicable(v);
    }
    
    public String toString() {
        return desc.testSpecUrl.toString();
    }
    
    public void run(TestResult result) {
        super.run(result);
        
        // to release unused resources.
        // this should allow GC to reclaim all the generated classes.
        compileTest.dispose();
    }

}
