/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.id;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.grammar.xducer.DeserializerContext;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;
import com.sun.tools.xjc.grammar.xducer.SerializerContext;

/**
 * {@link Transducer} for the ID datatype.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class IDTransducer extends IdentityTransducer
{
    public IDTransducer( JCodeModel _codeModel, SymbolSpace _symbolSpace ) {
        super(_codeModel);
        this.symbolSpace = _symbolSpace;
    }
    
    private final SymbolSpace symbolSpace;
    
    public boolean isID() { return true; }
    public SymbolSpace getIDSymbolSpace() { return symbolSpace; }
    
    public JExpression generateDeserializer( JExpression literal, DeserializerContext context ) {
        return context.addToIdTable(literal);
    }

    public JExpression generateSerializer( JExpression value, SerializerContext context ) {
        return context.onID(JExpr._this(),value);
    }
}
