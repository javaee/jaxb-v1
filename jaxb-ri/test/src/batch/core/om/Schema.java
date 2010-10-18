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
