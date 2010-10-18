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

package batch.core.compiler;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import batch.core.JavacException;
import batch.core.Util;
import batch.core.XJCException;
import batch.core.om.Schema;

/**
 * Compiler that uses XJC command line syntax.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
abstract class AbstractXJCCompilerImpl implements Compiler {

    /** Additional compiler parameters. */
    private final Collection xjcParams;

    public AbstractXJCCompilerImpl( Collection xjcParams ) {
        this.xjcParams = xjcParams;
    }

    public AbstractXJCCompilerImpl() {
        this.xjcParams = new ArrayList();
    }

    public final ClassLoader compile(Schema schema) throws XJCException, JavacException {
        // put additional schemas as "extra parameters."
        List params = new ArrayList(xjcParams);

        params.add(schema.schema.toExternalForm());
        params.add("-d");
        params.add(schema.outDir.getPath());
        
        params.add("-debug");   // so that ObjectFactory will be created at root.

        if(schema.targetPackageName!=null) {
            params.add("-p");
            params.add(schema.targetPackageName);
        }

    
        if(!schema.strictMode)
            params.add("-extension");
    
        // external binding files
        for( int i=0; i<schema.bindingFiles.length; i++ ) {
            params.add( "-b" );
            params.add( schema.bindingFiles[i].toExternalForm() );
        }
    
        // language
        String name = schema.schema.toExternalForm();
        if( name.endsWith(".dtd") )
            params.add("-dtd");
        if( name.endsWith(".rng") )
            params.add("-relaxng");
    
        params.addAll(schema.xjcOpts);
    
        // since we have the -debug switch, XJC will display
        // a lot of details about error to stdout. Also, we
        // have some negatie tests where stack trace is expected to be displayed.
        //  
        // to keep the progress report terse,
        // redirect the error stream to stdout.
        PrintStream defaultErr = System.err;
        try {
            System.setErr(System.out);
            invoke( (String[]) params.toArray(new String[params.size()]) );
        } finally {
            System.setErr(defaultErr);
        }
    
        try {
            return Util.compile(schema.parentClassLoader,schema.outDir);
        } catch( Exception e ) {
            throw new JavacException(e);
        }
    }
    
    /**
     * Invokes the compiler and waits for its completion.
     * 
     * @throws XJCException
     *      if the compiler fails to compile the schema
     */
    protected abstract void invoke( String[] args ) throws XJCException;
}
