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
package batch.core;

import javax.xml.bind.JAXBContext;

import junit.framework.TestCase;
import batch.core.compiler.Compiler;
import batch.core.om.Schema;


/**
 * JUnit {@link TestCase} that compiles a schema by using XJC and javac.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class CompileTestCase extends TestCase {
    
    /** schema to be compiled. */
    public final Schema schema;
    
    /** compiler to be used. */
    public final Compiler compiler;
    
    /**
     * If the schema is compiled and we are asked to create a
     * context, we will keep it here to avoid redundant context
     * creation.
     * 
     * <p>
     * This laziness also helps the performance test; some of
     * them need to measure the time it takes to create a new
     * context, so we don't want to do that for them automatically!  
     */
    private JAXBContext context = null;
    
    public CompileTestCase( Schema _schema, Compiler _compiler ) {
        super("compiling "+_schema.schema.toExternalForm());
        this.schema = _schema;
        this.compiler = _compiler;
    }

    /**
     * Set this flag to false if you want to skip XJC invocation.
     * This is useful to quickly re-run a test.
     */
    public boolean forceCompile = true;
    
    public void runTest() throws Exception {
        System.out.println("running "+super.getName());
        
        // TODO: instead of checking outDir.exists(), this should compare
        // the date of schema file and the JAXBContextImpl class in the outDir.
        if(forceCompile || !schema.outDir.exists()) {
            // runs XJC and compile the schema.
            System.out.println("generating class files");
            
            Util.recursiveDelete(schema.outDir);
            schema.outDir.mkdir();
            
            try {
                loader = compiler.compile(schema);
            } catch( XJCException e ) {
                if( schema.isNegativeTest )
                    return; // as expected
                
                throw e;    // failure.
            }
            
            assertTrue( "this test case is expected to fail", !schema.isNegativeTest );
        } else {
            System.out.println("skipping compilation");
            loader = Util.createClassLoader(schema.parentClassLoader,schema.outDir);
        }
        
        assertNotNull(loader);
    }
    
    /**
     * If the compilation is successful, this method returns a
     * reference to the generated JAXBContext. Otherwise return null.
     */
    public JAXBContext getContext() {
        if( context==null ) {
            try {
                // load the context
                context = (JAXBContext)loader.loadClass("ObjectFactory").newInstance();
            } catch (InstantiationException e) {
                throw new InstantiationError(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new NoClassDefFoundError(e.getMessage());
            }
        }
        return context;
    }
    
    private ClassLoader loader;
    public ClassLoader getLoader() { return loader; }
    
    /**
     * Releases references to other objects so that GC can reclaim
     * the memory space.
     */
    public void dispose() {
        context = null;
        loader = null;
    }
        
    public String toString() {
        return getName();
    }
}
