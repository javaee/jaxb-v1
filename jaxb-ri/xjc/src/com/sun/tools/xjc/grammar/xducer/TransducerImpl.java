/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JExpression;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.id.SymbolSpace;

/**
 * Partial abstract implementation of Transducer.
 * 
 * This class provides default implementations for many methods.
 */
public abstract class TransducerImpl implements Transducer
{
    public void populate(AnnotatedGrammar grammar, GeneratorContext context) {
    }
    
    public void declareNamespace( BlockReference body, JExpression value, SerializerContext context ) {
    }
    
    public boolean needsDelayedDeserialization() { return false; }
    public boolean isID() { return false; }
    public SymbolSpace getIDSymbolSpace() { return null; }
    public boolean isBuiltin() { return false; }

    public String toString() {
        String className = this.getClass().getName();
        int idx = className.lastIndexOf('.');
        if(idx>=0)  className = className.substring(idx+1);
        return  className+":"+getReturnType().name();
    }

    /**
     * Computes a string representation from an
     * expression that only contains &lt;value> and
     * no &lt;data>.
     * 
     * Used for the generateConstant method.
     */
    protected final String obtainString( ValueExp exp ) {
        return ((XSDatatype)exp.dt).convertToLexicalValue(exp.value,null);
    }
}
