/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * Wrap another Transducer and do a simple casting to change the return
 * type.
 * 
 * Since this class only does a Java casting, it's applicable only to
 * Java primitive types.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class CastTranducer extends TransducerDecorator {
    
    private final JPrimitiveType type;
    
    /**
     * Adapts "core" transducer to return the type specified by the
     * "type" parameter.
     */
    public CastTranducer( JPrimitiveType _type, Transducer _core ) {
        super(_core);
        this.type = _type;
        if( !super.getReturnType().isPrimitive())
            throw new JAXBAssertionError();  // assertion failure. must be a primitive type.
    }
    
    public JType getReturnType() {
        return type;
    }

    public JExpression generateSerializer(JExpression value, SerializerContext context) {
        return super.generateSerializer( JExpr.cast( super.getReturnType(), value ), context );
    }

    public JExpression generateDeserializer(JExpression literal, DeserializerContext context) {
        return JExpr.cast( type, super.generateDeserializer(literal,context) );
    }
    
    public void declareNamespace(BlockReference body, JExpression value, SerializerContext context) {
        super.declareNamespace(body,JExpr.cast( super.getReturnType(), value ),context);
    }

    public JExpression generateConstant(ValueExp exp) {
        return JExpr.cast( type, super.generateConstant(exp) );
    }
}
