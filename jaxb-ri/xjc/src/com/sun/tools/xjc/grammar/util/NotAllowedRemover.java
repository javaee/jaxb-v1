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
