/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.util;

import java.util.Set;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.DataExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.relax.NoneType;

/**
 * completely removes &lt;notAllowed /&gt; from the grammar.
 * 
 * The ExpressionPool class does a reasonable job to remove &lt;notAllowed/&gt;.
 * For example, the createSequence method returns Expression.nullSet if one of the 
 * parameter is the nullSet.
 * 
 * However, it cannot remove elements/attributes whose content model is the nullSet,
 * nor can it remove ReferenceExps whose body is the nullSet. This class walks the
 * grammar and removes those unused ReferenceExps, elements, and attributes.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class NotAllowedRemover extends ExpressionCloner {
    
    public NotAllowedRemover( ExpressionPool pool ) {
        super(pool);
    }
    
    public Expression onRef( ReferenceExp exp ) {
        if( !visitedExps.contains(exp) ) {
            // we cannot add expression before we finished modifying the exp.
            // when two refexps refer to each other,
            // a->b->a,
            exp.exp = exp.exp.visit(this);
            visitedExps.add(exp);
        }
        
        if(exp.exp==Expression.nullSet)
            return Expression.nullSet;
        
        return exp;
    }
    
    public Expression onOther( OtherExp exp ) {
        if( !visitedExps.contains(exp) ) {
            // we cannot add expression before we finished modifying the exp.
            exp.exp = exp.exp.visit(this);
            visitedExps.add(exp);
        }
        
       if(exp.exp==Expression.nullSet)
            return Expression.nullSet;
        
        return exp;
    }
    
    /**
     * this set keeps the visited expressions, to prevent
     * infinite recursion.
     */
    private final Set visitedExps = new java.util.HashSet();
    
    public Expression onElement( ElementExp exp ) {
        if( !visitedExps.add(exp) )
            return exp;    // this ElementExp is already processed.
        
        Expression body = exp.contentModel.visit(this);
        if( body==Expression.nullSet )
            return Expression.nullSet;
        
        exp.contentModel=body;
        return exp;
    }

    public Expression onAttribute( AttributeExp exp ) {
        if( !visitedExps.add(exp) )
            return exp;    // this AttributeExp is already processed.
        
        Expression body = exp.exp.visit(this);
        if( body==Expression.nullSet )
            return Expression.nullSet;
        
        return pool.createAttribute( exp.nameClass, body );
    }
    
    public Expression onData( DataExp exp ) {
        // treat "none" type as <notAllowed/>.
        if(exp.dt instanceof NoneType)
            return Expression.nullSet;
        else
            return exp;
    }
}
