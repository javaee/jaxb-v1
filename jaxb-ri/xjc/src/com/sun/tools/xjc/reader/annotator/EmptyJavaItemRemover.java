/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.annotator;

import java.util.Set;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.SuperClassItem;

/**
 * Removes SuperClassItem and FieldItem whose contents is empty.
 * 
 * <p>
 * Sometimes, in particular when wildcards are used, a FieldItem can
 * have empty contents. For example, consider the following example
 * taken from WSDL.
 * 
 * <pre><xmp>
 * <complexType name="tExtensibleAttributesDocumented">
 *   <complexContent>
 *     <extension base = "wsdl:tDocumented">
 *       <annotation>
 *         <documentation>
 *           This type is extended by  component types
 *           to allow attributes from other namespaces to be added.
 *         </documentation>
 *       </annotation>
 *       <anyAttribute namespace="##other"/>
 *     </extension>
 *   </complexContent>
 * </complexType>
 * </xmp></pre>
 * 
 * <p>
 * This attribute wildcard declaration is effectively empty-ified because
 * no global attribute declarations are made in the other namespace.
 * 
 * <p>
 * Hence the field item that corresponds to this wildcard will become empty.
 * If a FieldItem becomes empty, it confuses XJC because it has no valid
 * contents in it. (For example, XJC cannot compute the type of this field.)
 * 
 * <p>
 * This step removes these problematic FieldItems.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class EmptyJavaItemRemover extends ExpressionCloner {
    
    public EmptyJavaItemRemover( ExpressionPool pool ) {
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
        
        if(exp.exp==Expression.epsilon)
            return Expression.epsilon;
        
        return exp;
    }
    
    public Expression onOther( OtherExp exp ) {
        if( !visitedExps.contains(exp) ) {
            // we cannot add expression before we finished modifying the exp.
            exp.exp = exp.exp.visit(this);
            visitedExps.add(exp);
        }
        
        if((exp instanceof SuperClassItem) || (exp instanceof FieldItem))
           if(exp.exp==Expression.epsilon)
               return Expression.epsilon;
        
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
        
        exp.contentModel=exp.contentModel.visit(this);
        return exp;
    }

    public Expression onAttribute( AttributeExp exp ) {
        if( !visitedExps.add(exp) )
            return exp;    // this AttributeExp is already processed.
        
        return pool.createAttribute( exp.nameClass, exp.exp.visit(this) );
    }
}
