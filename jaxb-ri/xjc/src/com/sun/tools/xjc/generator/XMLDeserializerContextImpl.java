/*
 * @(#)$Id: XMLDeserializerContextImpl.java,v 1.1 2004-06-25 21:14:16 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator;

import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.grammar.xducer.DeserializerContext;

/**
 * {@link com.sun.tools.xjc.grammar.xducer.DeserializerContext}
 * implemented for {@link com.sun.tools.xjc.runtime.UnmarshallingContext}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class XMLDeserializerContextImpl implements DeserializerContext {
    
    /**
     * A variable reference to {@link com.sun.tools.xjc.runtime.UnmarshallingContext}.
     */
    private final JExpression $context;
    
    public XMLDeserializerContextImpl( JExpression  _$context ) {
        this.$context = _$context;
    }
    
    /**
     * Returns the reference to the context object itself.
     */
    public JExpression ref() {
         return $context;
    }
    
    public JExpression addToIdTable(JExpression literal) {
        return $context.invoke("addToIdTable").arg(literal);
    }

    public JExpression getObjectFromId(JExpression literal) {
        return $context.invoke("getObjectFromId").arg(literal);
    }

    public JExpression getNamespaceContext() {
        return $context;
    }

}
