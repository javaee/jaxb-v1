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
