/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.id;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.util.WhitespaceNormalizer;
import com.sun.tools.xjc.grammar.xducer.DeserializerContext;
import com.sun.tools.xjc.grammar.xducer.SerializerContext;
import com.sun.tools.xjc.grammar.xducer.TransducerImpl;
import com.sun.xml.bind.marshaller.IdentifiableObject;

/**
 * {@link Transducer} for the ID datatype.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class IDREFTransducer extends TransducerImpl
{
    public IDREFTransducer( JCodeModel _codeModel, SymbolSpace _symbolSpace, boolean _whitespaceNormalization ) {
        this.codeModel = _codeModel;
        this.symbolSpace = _symbolSpace;
        this.whitespaceNormalization = _whitespaceNormalization;
    }
    
    private final JCodeModel codeModel;
    private final SymbolSpace symbolSpace;
    private final boolean whitespaceNormalization;
    
    /** Gets the symbol space to which this IDREF belongs to. */
    public SymbolSpace getIDSymbolSpace() {
        return symbolSpace;
    }
    
    public JType getReturnType() {
        return symbolSpace.getType();
    }
    
    public JExpression generateSerializer( JExpression literal, SerializerContext context ) {
        // [RESULT]
        //   context.onIDREF((IdentifiableObject)literal)
        return context.onIDREF(JExpr.cast(codeModel.ref(IdentifiableObject.class),literal));
    }
    
    public JExpression generateDeserializer( JExpression literal, DeserializerContext context ) {
        // [RESULT]
        //  (TYPE)context.getId(literal);
        return JExpr.cast(symbolSpace.getType(),
            context.getObjectFromId( whitespaceNormalization
                ?WhitespaceNormalizer.COLLAPSE.generate(codeModel,literal)
                :literal));
    }

    public boolean needsDelayedDeserialization() {
        // do a delayed de-serialization so that
        // we can correctly handle forward reference to IDs.
        return true;
    }

    public String toString() {
        return "IDREFTransducer:"+symbolSpace.toString();
    }
    
    
    public JExpression generateConstant( ValueExp exp ) {
        // waiting for a clarification from the spec
        // but most likelily this would be prohibited.
        throw new UnsupportedOperationException(
            Messages.format( Messages.CONSTANT_IDREF_ERROR ) );
    }
    
}
