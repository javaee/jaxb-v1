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