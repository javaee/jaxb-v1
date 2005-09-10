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
