/*
 * @(#)$Id: Schema.java,v 1.1 2004-06-25 21:13:01 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.core.om;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import batch.core.Util;

/**
 * Information about the schema to be compiled.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class Schema {
    
    
    /** temporary directory to be used to generate Java sources/binaries. */
    public final File outDir;
    
    /** Schema file(s) to be compiled. */
    public final URL schema;
    
    /** External binding files. */
    public final URL[] bindingFiles;
    
    /**
     * The package name to which generated files belong.
     * 
     * Can be null.
     */
    public final String targetPackageName;
    
    /** True to compile the schema under the strict mode. */
    public final boolean strictMode;
    
    /** True if the compilation is expected to fail. */
    public final boolean isNegativeTest;
    
    /** Additional XJC command line options. */
    public final List xjcOpts;
    
    /**
     * The class loader that loads the compiled classes will be
     * hung under this class loader. Never be null. 
     */
    public final ClassLoader parentClassLoader;
    
    /**
     * @param _parent
     *      can be null.
     */
    public Schema( URL schema, URL[] bindings, File outDir,
        String targetPackageName, boolean strict, boolean negativeTest, ClassLoader _parent, List xjcOpts ) {
        
        this.schema = schema;
        this.bindingFiles = bindings;
        this.outDir = outDir;
        this.targetPackageName = targetPackageName;
        this.strictMode = strict;
        this.isNegativeTest = negativeTest;
        if(_parent==null)
            _parent = Util.getForeheadClassLoader("object");
        this.parentClassLoader = _parent;
        this.xjcOpts = new ArrayList(xjcOpts);
    }
}