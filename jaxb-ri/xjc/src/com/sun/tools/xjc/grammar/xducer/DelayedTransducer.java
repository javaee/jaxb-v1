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

/**
 * Wrapper of a Transducer that defers the instanciation of the actual
 * transducer.
 * 
 * <p>
 * To use this transducer, derive a new class from it and implement
 * the create method.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class DelayedTransducer implements Transducer {
    
    private Transducer core = null;
    
    /**
     * Create a transducer object, which will be then used
     * to implement the Transducer semantics.
     * 
     * This method is called once when one of the methods is called
     * from clients.
     * 
     * @return
     *      Always return non-null valid object.
     */
    protected abstract Transducer create();
    
    /**
     * Ensure <code>core!=null</code>.
     */
    private void update() {
        if( core==null )        core = create();
    }
    
    public JType getReturnType() {
        update();
        return core.getReturnType();
    }

    public boolean isBuiltin() {
        return false;
    }

    public JExpression generateSerializer(JExpression value, SerializerContext context) {
        update();
        return core.generateSerializer(value,context);
    }

    public JExpression generateDeserializer(JExpression literal, DeserializerContext context) {
        update();
        return core.generateDeserializer(literal,context);
    }

    public JExpression generateConstant(ValueExp exp) {
        update();
        return core.generateConstant(exp);
    }

    public void declareNamespace(BlockReference body, JExpression value, SerializerContext context) {
        update();
        core.declareNamespace(body,value,context);
    }

    public boolean needsDelayedDeserialization() {
        update();
        return core.needsDelayedDeserialization();
    }

    public void populate(AnnotatedGrammar grammar, GeneratorContext context) {
        update();
        core.populate(grammar,context);
    }

}
