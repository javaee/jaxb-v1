/*
 * @(#)$Id: AbstractXJCCompilerImpl.java,v 1.1 2004-06-25 21:13:00 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
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
