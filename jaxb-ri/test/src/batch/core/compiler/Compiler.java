/*
 * @(#)$Id: Compiler.java,v 1.1 2004-06-25 21:13:00 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.core.compiler;

import batch.core.JavacException;
import batch.core.XJCException;
import batch.core.om.Schema;

/**
 * JAXB Compiler interface.
 * 
 * This interface is used to allow unit tests to be run
 * against different kinds of compilers in different ways.
 * 
 * (Such as running XJC in memory, or invoking external
 * XJCs)
 *  
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface Compiler {
    /**
     * Compiles the schema under the specified setting and
     * returns the {@link ClassLoader} that can load compiled classes.
     * 
     * @throws XJCException
     *      if the schema compilation fails.
     * @throws JavacException
     *      if the compilation of the generated source code fails.
     * 
     * @return
     *      non-null valid object.
     */
    ClassLoader compile( Schema schema ) throws XJCException, JavacException;
}
