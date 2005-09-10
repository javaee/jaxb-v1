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

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.id.SymbolSpace;

/**
 * Filter implementation of a {@link Transducer} that passes
 * through every method call to another Transducer.
 * 
 * The derived class is expected to override methods and add
 * some meaningful semantics.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class TransducerDecorator implements Transducer {
    
    /** Next transducer in the chain. */
    protected final Transducer core;
    
    protected TransducerDecorator( Transducer _core ) {
        this.core = _core;
    }
    
    public JType getReturnType() {
        return core.getReturnType();
    }

    public boolean isBuiltin() {
        // decorating another transducer will usually render the conversion non-standard.
        return false;
    }

    public void populate(AnnotatedGrammar grammar, GeneratorContext context) {
        core.populate(grammar,context);
    }

    public JExpression generateSerializer(JExpression value, SerializerContext context) {
        return core.generateSerializer(value,context);
    }

    public void declareNamespace(BlockReference body, JExpression value, SerializerContext context) {
        core.declareNamespace(body,value,context);
    }

    public JExpression generateDeserializer(JExpression literal, DeserializerContext context) {
        return core.generateDeserializer(literal,context);
    }

    public boolean needsDelayedDeserialization() {
        return core.needsDelayedDeserialization();
    }
    
    public boolean isID() {
        return core.isID();
    }
    public SymbolSpace getIDSymbolSpace() {
        return core.getIDSymbolSpace();
    }

    public JExpression generateConstant(ValueExp exp) {
        return core.generateConstant(exp);
    }

}
