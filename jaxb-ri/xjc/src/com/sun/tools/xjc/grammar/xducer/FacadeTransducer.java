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
 * Combines two transducers of the same return type so that
 * one is used for marshalling while the other is used for unmarshalling.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class FacadeTransducer implements Transducer {
    
    /** Transducer for marshalling. */
    private final Transducer marshaller;
    /** Transducer for unmarshalling. */
    private final Transducer unmarshaller;
    
    public FacadeTransducer( Transducer _marshaller, Transducer _unmarshaller ) {
        this.marshaller = _marshaller;
        this.unmarshaller = _unmarshaller;
        
//        if( marshaller.getReturnType()!=unmarshaller.getReturnType() )
//            throw new InternalError();  // assertion failed
    }
    
    public JType getReturnType() {
        return marshaller.getReturnType();
        // which is by assumption equivalent to unmarshaller.getReturnType
    }

    public boolean isID() { return false; }
    public SymbolSpace getIDSymbolSpace() { return null; }

    public boolean isBuiltin() {
        return false;
    }

    public void populate(AnnotatedGrammar grammar, GeneratorContext context) {
        marshaller  .populate(grammar,context);
        unmarshaller.populate(grammar,context);
    }

    public JExpression generateSerializer(JExpression value, SerializerContext context) {
        return marshaller.generateSerializer(value,context);
    }

    public void declareNamespace(BlockReference body, JExpression value, SerializerContext context) {
        marshaller.declareNamespace(body,value,context);
    }

    public JExpression generateDeserializer(JExpression literal, DeserializerContext context) {
        return unmarshaller.generateDeserializer(literal,context);
    }

    public boolean needsDelayedDeserialization() {
        return unmarshaller.needsDelayedDeserialization();
    }

    public JExpression generateConstant(ValueExp exp) {
        return unmarshaller.generateConstant(exp);
    }

}
