/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * Use is subject to the license terms.
 */
package com.sun.tools.xjc.generator.cls;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.MethodWriter;
import com.sun.tools.xjc.grammar.ClassItem;

/**
 * 
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface ImplStructureStrategy {
    
    /**
     * Determines the location of the implementation class and
     * creates one.
     */
    JDefinedClass createImplClass( ClassItem targetClass );
    
    MethodWriter createMethodWriter( ClassContext target );
}
