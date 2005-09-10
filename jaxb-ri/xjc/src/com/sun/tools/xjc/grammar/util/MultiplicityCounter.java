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
package com.sun.tools.xjc.grammar.util;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.ConcurExp;
import com.sun.msv.grammar.DataExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionVisitor;
import com.sun.msv.grammar.InterleaveExp;
import com.sun.msv.grammar.ListExp;
import com.sun.msv.grammar.MixedExp;
import com.sun.msv.grammar.OneOrMoreExp;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.SequenceExp;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.JavaItem;

/**
 * visits an expression and computes the multiplicity of the designated child item.
 * 
 * Note that currently this method does not do anything to stop infinite recursion.
 * The algorithm should be implemented within this class to handle recursive multiplicity
 * calculation.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public abstract class MultiplicityCounter implements ExpressionVisitor
{
    /**
     * computes the multiplicity of child Java items.
     */
    public static final MultiplicityCounter javaItemCounter =
        new MultiplicityCounter(){
            protected Multiplicity isChild( Expression exp ) {
                if(exp instanceof IgnoreItem) return Multiplicity.zero;
                if(exp instanceof JavaItem)    return Multiplicity.one;
                else                        return null;
            }
        };
    
    /**
     * this method should be implemented for this class to work correctly.
     * If the specified expression is a designated child item, then
     * return its multiplicity. Otherwise return null.
     */
    protected abstract Multiplicity isChild( Expression exp );
    
    public Object onSequence( SequenceExp exp ) {
        Multiplicity m = isChild(exp);
        if(m!=null)    return m;
        
        return Multiplicity.group(
            (Multiplicity)exp.exp1.visit(this),
            (Multiplicity)exp.exp2.visit(this)
        );
    }
    
    public Object onInterleave( InterleaveExp exp ) {
        Multiplicity m = isChild(exp);
        if(m!=null)    return m;
        
        return Multiplicity.group(
            (Multiplicity)exp.exp1.visit(this),
            (Multiplicity)exp.exp2.visit(this)
        );
    }
    
    public Object onChoice( ChoiceExp exp ) {
        Multiplicity m = isChild(exp);
        if(m!=null)    return m;
        
        return Multiplicity.choice(
            (Multiplicity)exp.exp1.visit(this),
            (Multiplicity)exp.exp2.visit(this)
        );
    }
    
    public Object onOneOrMore( OneOrMoreExp exp ) {
        Multiplicity m = isChild(exp);
        if(m!=null)    return m;
        return Multiplicity.oneOrMore( (Multiplicity)exp.exp.visit(this) );
    }
    
    public Object onMixed( MixedExp exp ) {
        Multiplicity m = isChild(exp);
        if(m!=null)    return m;
        return exp.exp.visit(this);
    }
    
    public Object onList( ListExp exp ) {
        Multiplicity m = isChild(exp);
        if(m!=null)    return m;
        return exp.exp.visit(this);
    }
    
    public Object onEpsilon() {
        Multiplicity m = isChild(Expression.epsilon);
        if(m==null) m=Multiplicity.zero;
        return m;
    }
    
    public Object onAnyString() {
        Multiplicity m = isChild(Expression.anyString);
        if(m==null) m=Multiplicity.zero;
        return m;
    }
    
    public Object onData( DataExp exp ) {
        Multiplicity m = isChild(exp);
        if(m==null) m=Multiplicity.zero;
        return m;
    }
    
    public Object onValue( ValueExp exp ) {
        Multiplicity m = isChild(exp);
        if(m==null) m=Multiplicity.zero;
        return m;
    }
    
    public Object onElement( ElementExp exp ) {
        Multiplicity m = isChild(exp);
        if(m!=null) return m;
        return exp.contentModel.visit(this);
    }
    
    public Object onAttribute( AttributeExp exp ) {
        Multiplicity m = isChild(exp);
        if(m!=null) return m;
        return exp.exp.visit(this);
    }
    
    public Object onRef( ReferenceExp exp ) {
        Multiplicity m = isChild(exp);
        if(m!=null) return m;
        return exp.exp.visit(this);
    }

    public Object onOther( OtherExp exp ) {
        Multiplicity m = isChild(exp);
        if(m!=null) return m;
        return exp.exp.visit(this);
    }
    
// assertions. these method may never be called.
    public Object onConcur( ConcurExp exp ) { throw new Error(); }
    public Object onNullSet()                { throw new Error(); }
}
