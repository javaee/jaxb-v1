/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.generator.util.BlockReference;

/**
 * Performs conversion between primitive types and wrapper types,
 * if necessary.
 * 
 * <p>
 * This converter is used to absorb the type difference between
 * transducers and field renderers.
 * 
 * <p>
 * In general, we try to consider the boxed type and the unboxed type to be
 * equivalent as much as possible. Therefore, the type adoption happens
 * only at the last minute when we actually generate the code.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class TypeAdaptedTransducer extends TransducerDecorator {
    /**
     * Wrap a tranducer and adapt the return type to
     * that of the field renderer, if necessary.
     */
    public static Transducer adapt( Transducer xducer, FieldRenderer fieldRenderer ) {
        return adapt( xducer, fieldRenderer.getFieldUse().type );
    }
        
    /**
     * Wrap a tranducer and adapt the return type of it if necessary
     */
    public static Transducer adapt( Transducer xducer, JType expectedType ) {
        
        JType t = xducer.getReturnType();
        if( t instanceof JPrimitiveType && expectedType instanceof JClass ) {
            expectedType = ((JPrimitiveType)t).getWrapperClass();
            return new TypeAdaptedTransducer(xducer,expectedType);      // use adaptor
        }
        
        
        if( t instanceof JClass && expectedType instanceof JPrimitiveType )
            return new TypeAdaptedTransducer(xducer,expectedType);      // use adaptor
        
        // no adaptor.
        return xducer;
    }
    
    
    /** The return type from this type-adapted transducer. */
    private final JType expectedType;
    
    private final boolean boxing;
    
    private TypeAdaptedTransducer( Transducer _xducer, JType _expectedType ) {
        super(_xducer);
        this.expectedType = _expectedType;
        boxing = (expectedType instanceof JClass);
    }
    
            
    public JType getReturnType() {
        return expectedType;
    }


    public JExpression generateSerializer(JExpression value, SerializerContext context) {
        if( boxing )
            return super.generateSerializer(
                ((JPrimitiveType)super.getReturnType()).unwrap(value), context );
        else
            return super.generateSerializer(
                ((JPrimitiveType)expectedType).wrap(value), context );
    }


    public JExpression generateDeserializer(JExpression literal, DeserializerContext context) {
        if( boxing )
            return ((JPrimitiveType)super.getReturnType()).wrap(
                super.generateDeserializer(literal,context));
        else
            return ((JPrimitiveType)expectedType).unwrap(
                super.generateDeserializer(literal,context));
    }

    public JExpression generateConstant(ValueExp exp) {
        if( boxing )
            return ((JPrimitiveType)super.getReturnType()).wrap(
                super.generateConstant(exp));
        else
            return ((JPrimitiveType)expectedType).unwrap(
                super.generateConstant(exp));
    }
    
    public void declareNamespace(BlockReference body, JExpression value, SerializerContext context) {
        if( boxing )
            super.declareNamespace(body,
                ((JPrimitiveType)super.getReturnType()).unwrap(value),context);
        else
            super.declareNamespace(body,
                ((JPrimitiveType)expectedType).wrap(value), context );
    }
}
