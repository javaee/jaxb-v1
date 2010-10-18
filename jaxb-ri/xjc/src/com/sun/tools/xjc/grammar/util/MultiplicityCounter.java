/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
