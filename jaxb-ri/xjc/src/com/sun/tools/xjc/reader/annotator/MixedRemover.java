/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.annotator;

import java.util.Set;

import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.MixedExp;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;

/**
 * Replaces {@link MixedExp} with interlave.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class MixedRemover extends ExpressionCloner {
    
    /**
     * this set keeps the visited expressions, to prevent
     * infinite recursion.
     */
    private final Set visitedExps = new java.util.HashSet();
    
    
    private final AnnotatedGrammar grammar;
    
    public MixedRemover( AnnotatedGrammar g) {
        super(g.getPool());
        this.grammar = g;
    }
    
    public Expression onRef( ReferenceExp exp ) {
        if( visitedExps.add(exp) )
            exp.exp = exp.exp.visit(this);
        
        return exp;
    }
    
    public Expression onOther( OtherExp exp ) {
        if( visitedExps.add(exp) ) {
            exp.exp = exp.exp.visit(this);
        }
        
        return exp;
    }
    
    public Expression onElement( ElementExp exp ) {
        if( !visitedExps.add(exp) )
            return exp;    // this ElementExp is already processed.
        
        exp.contentModel=exp.contentModel.visit(this);
        return exp;
    }

    public Expression onAttribute( AttributeExp exp ) {
        if( !visitedExps.add(exp) )
            return exp;    // this AttributeExp is already processed.
        
        return pool.createAttribute( exp.nameClass, exp.exp.visit(this) );
    }
    
    public Expression onMixed( MixedExp exp ) {
        /*
        expand this <mixed> as
        <interleave>
            <zeroOrMore>
                <primitiveItem>
                    <data type="string"/>
                </primitiveItem>
            </zeroOrMore>
            exp.exp
        </interleave>
        */
        return pool.createInterleave(
                pool.createZeroOrMore(
                    grammar.createPrimitiveItem(
                        new IdentityTransducer(grammar.codeModel),
                        StringType.theInstance, // no guard
                        pool.createData(StringType.theInstance),
                        null )),
                exp.exp.visit(this) );
    }
}
