/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
