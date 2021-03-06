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
