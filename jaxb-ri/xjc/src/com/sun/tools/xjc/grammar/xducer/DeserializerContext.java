/*
 * @(#)$Id: DeserializerContext.java,v 1.1 2004-06-25 21:14:46 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JExpression;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface DeserializerContext {
    /**
     * Registers the literal value as an ID to the object currently
     * being unmarshalled.
     */
    JExpression addToIdTable( JExpression literal );
    
    /**
     * Resolves an object from its ID value.
     */
    JExpression getObjectFromId( JExpression literal );

    
    /**
     * Returns the expression that evaluates to
     * a {@link javax.xml.namespace.NamespaceContext} object.
     * <p>
     * For example, the object returned will be used to declare
     * new namespaces for QName.
     */
    JExpression getNamespaceContext();
}
