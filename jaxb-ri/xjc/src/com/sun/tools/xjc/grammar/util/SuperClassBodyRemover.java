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
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.JavaItem;
import com.sun.tools.xjc.grammar.SuperClassItem;

/**
 * removes ClassItem inside SuperClassItem as a preparation
 * of the marshaller generation.
 * 
 * <p>
 * Care has to be taken not to remove ClassItems directly referenced
 * from other part of the grammar. For example,
 * 
 * <PRE><XMP>
 * <start>
 *   <choice>
 *     <element name="derived" t:role="class">
 *       <ref name="body" t:role="superClass"/>
 *     </element>
 *     <element name="base">
 *       <ref name="body"/>
 *     </element>
 *   </choice>
 * </start>
 * 
 * <define name="body" t:role="class">
 *   <data type="string"/>
 * </define>
 * </XMP></PRE>
 * 
 * We can't simply remove ClassItem from "body". Intuitively, we are
 * to create the following pattern from the above pattern in this process.
 * 
 * <PRE><XMP>
 * <start>
 *   <choice>
 *     <element name="derived" t:role="class">
 *       <ref name="body1" t:role="superClass"/>
 *     </element>
 *     <element name="base">
 *       <ref name="body2"/>
 *     </element>
 *   </choice>
 * </start>
 * 
 * <define name="body1">
 *   <data type="string"/>
 * </define>
 * 
 * <define name="body2" t:role="class">
 *   <data type="string"/>
 * </define>
 * </XMP></PRE>
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class SuperClassBodyRemover extends ExpressionCloner {
    
    private final Set visitedRefs = new java.util.HashSet();
    
    public static void remove( AnnotatedGrammar g ) {
        SuperClassBodyRemover su = new SuperClassBodyRemover(g.getPool());
        
        ClassItem[] cls = g.getClasses();
        for( int i=0; i<cls.length; i++ )
            cls[i].exp = cls[i].exp.visit(su);
    }
    
    public Expression onAttribute( AttributeExp exp ) {
        return pool.createAttribute( exp.nameClass, exp.exp.visit(this) );
    }
    
    public Expression onElement( ElementExp exp ) {
        if(visitedRefs.add(exp))
            exp.contentModel = exp.contentModel;
        return exp;
    }
    
    public Expression onRef( ReferenceExp exp ) {
        if(visitedRefs.add(exp))
            exp.exp = exp.exp.visit(this);
        return exp;        // recurse children if this is the first visit.
    }
    
    public Expression onOther( OtherExp exp ) {
        if( exp instanceof SuperClassItem ) {
            return exp.exp.visit(remover);
        }
        if(visitedRefs.add(exp))
            exp.exp=exp.exp.visit(this);
        return exp;
    }
    
    private ExpressionCloner remover;

    
    private SuperClassBodyRemover( ExpressionPool pool ) {
        super(pool);
        remover = new ExpressionCloner(pool){
            
            public Expression onRef( ReferenceExp exp ) {
                return exp.exp.visit(this);
            }
            
            public Expression onOther( OtherExp exp ) {
                if( exp instanceof ClassItem ) {
                    // this is the definition of this super class item.
                    // remove it.
                    return exp.exp;
                }
                
                // it must not be a JavaItem.
                // this check should have already been done by the RelationNormalizer
                if( exp instanceof JavaItem )
                    throw new Error("internal error");
                
                // other unknown exps
                return exp.exp.visit(this);
            }
            
            /*
            we have to copy ElementExp/AttributeExp if that is necessary.
            consider the following pattern:
            <start>
                <choice>
                    <group t:role="class">
                        <ref name="body" t:role="superClass"/>
                        <element name="ext"/>
                    </group>
                    <ref name="body">
                </choice>
            </start>
            <define name="body">
                <element name="body">
                    <group t:role="class" id="base">
                        <data />
                    </group>
                </element>
            </define>
            
            We cannot strip the ClassItem of the "base" correctly unless
            we copy the definition of the "body" elment.
            */
            public Expression onAttribute( AttributeExp exp ) {
                return super.pool.createAttribute(exp.nameClass,exp.exp.visit(this));
            }
            
            public Expression onElement( ElementExp exp ) {
                Expression body = exp.contentModel.visit(this);
                if(body==exp.contentModel)    return exp;    // this item is not modified.
                else
                    // body of this element is modified.
                    // since this ElementExp might be shared,
                    // we need to create a fresh ElementExp.
                    return new ElementPattern(exp.getNameClass(),body);
            }
        };
    }
    
}
