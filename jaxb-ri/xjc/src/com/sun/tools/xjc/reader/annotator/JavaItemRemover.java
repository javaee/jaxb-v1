/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.annotator;

import java.util.Set;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ConcurExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;

/**
 * removes specified JavaItems.
 * <p>
 * removed all JavaItems contained in the specified set.
 */
class JavaItemRemover extends ExpressionCloner {

    /** debug logger. */
    private static java.io.PrintStream debug = null;
    
    JavaItemRemover( ExpressionPool pool, Set targets ) {
        super(pool);
        this.targets = targets;
    }
        
    /** ClassItems contained in this set will be removed by this procedure. */
    private final Set targets;
        
// assertions. these method may never be called.
    public Expression onNullSet()                            { throw new Error(); }
    public Expression onConcur( ConcurExp exp )                { throw new Error(); }

// attribute/element.
    public Expression onAttribute( AttributeExp exp ) {
        Expression body = exp.exp.visit(this);
        if( body==exp.exp )    return exp;
        else    return pool.createAttribute( exp.nameClass, body );
    }
    
    private final Set visitedExps = new java.util.HashSet();
    
    public Expression onElement( ElementExp exp ) {
        if( !visitedExps.add(exp) )
            // this exp is already processed. this check will prevent infinite recursion.
            return exp;
        exp.contentModel = exp.contentModel.visit(this);
        return exp;
    }
    
    public Expression onRef( ReferenceExp exp ) {
        if( !visitedExps.add(exp) )
            // this exp is already processed. this check will prevent infinite recursion.
            return exp;
        // update the definition and return self.
        exp.exp = exp.exp.visit(this);
        return exp;
    }
    
    public Expression onOther( OtherExp exp ) {
        if( targets.contains(exp) ) {
            if(debug!=null)
                debug.println( " "+exp+": found and removed");
                
            // this temporary class item is unnecessary. remove it.
            // but don't forget to recurse its descendants.
            return exp.exp.visit(this);
        }
            
        // update the definition and return self.
        exp.exp = exp.exp.visit(this);
        return exp;
    }
}
