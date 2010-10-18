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
