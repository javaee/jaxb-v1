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
