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
