/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.msv.grammar.Expression;
import com.sun.xml.xsom.XSComplexType;

/**
 * Builds a field expression from a complex type.
 * 
 * Depending on a "kind" of complex type, the binding is
 * quite different. For example, how a complex type is bound
 * when it is extended from another complex type is very
 * different from how it's bound when it has, say, mixed content model.
 * 
 * Each different algorithm of binding a complex type is implemented
 * as an implementation of this interface.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
interface CTBuilder {
    /**
     * Returns true if this builder can handle the given complex type.
     */
    boolean isApplicable( XSComplexType ct );
    
    /**
     * Binds the given complex type. This method will be called
     * only when the <code>isApplicable</code> method returns true.
     */
    Expression build( XSComplexType ct );

}
