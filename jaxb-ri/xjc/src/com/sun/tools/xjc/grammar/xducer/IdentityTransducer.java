/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;

/**
 * A transducer that does no conversion but the whitespace normalization.
 */
public class IdentityTransducer extends TransducerImpl
{
    /** reference to the "java.lang.String" class. */
    private final JClass stringType;
    
    public IdentityTransducer( JCodeModel codeModel ) {
        stringType = codeModel.ref(java.lang.String.class);
    }
    
    public JType getReturnType() { return stringType; }
    
    public JExpression generateSerializer( JExpression value, SerializerContext context ) {
        return value;
    }
    
    public JExpression generateDeserializer( JExpression literal, DeserializerContext context ) {
        return literal;
    }

    public JExpression generateConstant( ValueExp exp ) {
        return JExpr.lit(obtainString(exp));
    }
    
}
