/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader;

import java.util.Map;
import java.util.Set;

import com.sun.codemodel.JPackage;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.util.ExpressionWalker;

/**
 * Maintains association between packages and grammar expressions.
 * 
 * To generate enumeration classes into appropriate directories,
 * we need to be able to tell which package it should belong to.
 * However, since {@link Expression}s are shared and reused, this is
 * tricky to do.
 * 
 * Fortunately, {@link ReferenceExp} is not shared, and this can be
 * uniquely associated with a package. Any other expressions that
 * hang from a ReferenceExp can be considered as in the same package.
 * (since any declaration starts with a ReferenceExp.)
 * 
 * This property holds for all currently supported schema languages.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public final class HierarchicalPackageTracker implements PackageTracker
{
    /** A map from ReferenceExp to JPackage. */
    private final Map dic = new java.util.HashMap();
    
    /**
     * Gets the JPackage object which is associated to the specified ReferenceExp.
     * returns null if associateion is not found.
     */
    public final JPackage get( ReferenceExp exp ) {
        return (JPackage)dic.get(exp);
    }
    
    
    /**
     * DIRTY CODE: this variable is set at the beginning of the associate method,
     * and is used by the visitor object.
     */
    private JPackage pkg;
    
    /**
     * Visits an expression and register all newly discovered associations.
     */
    private final ExpressionWalker visitor = new ExpressionWalker() {
        /** records all visited elements and attributes to avoid unnecessary traversal. */
        private final Set visitedExps = new java.util.HashSet();
        
        public void onElement( ElementExp exp ) {
            if(!visitedExps.add(exp))   return;
            
            if(exp.contentModel!=null)
                exp.contentModel.visit(this);
        }
            
        public void onAttribute( AttributeExp exp ) {
            if(!visitedExps.add(exp))   return;
            
            if(exp.exp!=null)
                exp.exp.visit(this);
        }
        
        public void onRef( ReferenceExp exp ) {
            if(dic.containsKey(exp))
                return; // this ReferenceExp already has an assocation.
                        // do not override existing one.
                
            dic.put(exp,pkg);
                
            if(exp.exp!=null)
                // during the grammar parsing phase,
                // it is allowed to have null.
                exp.exp.visit(this);
        }
    };
    
    /** Associate an expression with JPackage. */
    public final void associate( Expression exp, JPackage _pkg ) {
        this.pkg = _pkg;
        exp.visit(visitor);
    }
}
