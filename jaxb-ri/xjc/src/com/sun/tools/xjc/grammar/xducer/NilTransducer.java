/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;

/**
 * Used to parse a boolean (of the value of the "xsi:nil" attribute
 * of XML Schema) into Java null value.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class NilTransducer extends TransducerImpl {
    
    public NilTransducer( JCodeModel _codeModel ) {
        this.codeModel = _codeModel;
    }
    
    private final JCodeModel codeModel;
    
    public JType getReturnType() {
        return codeModel.NULL;
    }

    public JExpression generateSerializer(JExpression value, SerializerContext context) {
        // this "true" is expected to be used as the value of the xsi:nil attribute.
        return JExpr.lit("true");
    }

    /** Deserializes null from a boolean value. */
    public JExpression generateDeserializer(JExpression literal, DeserializerContext context) {
        // since
        // <foo xsi:nil="false">5</foo> is allowed,
        // 
        // we need to check the value of xsi:nil before we unmarshal @xsi:nil.
        // validation of the attribute value is done at that time, so this doesn't 
        // need to consult the literal at all.
        return JExpr._null();
    }

    public JExpression generateConstant( ValueExp exp ) {
        // nil transducer is only used for xsi:nil, so it will never be 
        // a subject to the constant property customization.
        throw new UnsupportedOperationException();
    }

}
