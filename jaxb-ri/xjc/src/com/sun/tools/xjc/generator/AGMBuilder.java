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

package com.sun.tools.xjc.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.Grammar;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.xml.bind.GrammarImpl;

/**
 * Construct an AGM for the whole grammar by combining pieces from
 * the ClassItem.getAGM method.
 * 
 * <p>
 * The result is used to perform unmarshal time validation.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class AGMBuilder extends ExpressionCloner
{
    /** from ClassItem to their AGMFragment wrapped in a ReferenceExp. */
    private final Map class2agm = new HashMap();
    
    /**
     * From each {@link ReferenceExp} in the original grammar
     * to its processed form {@link Expression}.
     */
    private final Map ref2exp = new HashMap();
    
    /**
     * From each {@link ElementExp} in the original grammar
     * to its processed form {@link ElementExp}.
     */
    private final Map elem2exp = new HashMap();
    
    /**
     * <pre><xmp>
     * <define name="anyContent">
     *   <zeroOrMore>
     *     <choice>
     *       <element>
     *         <anyName/>
     *         <ref name="anyContent"/>
     *       </element>
     *       <attribute>
     *         <anyName/>
     *         <text/>
     *       </anyName>
     *       <text/>
     *     </choice>
     *   </zeroOrMore>
     * </define>
     */
    private final ReferenceExp anyContent;
    
    /**
     * Plug objects that will be associated with the grammar.
     */
    private final ArrayList plugs = new ArrayList();
    
    private final GrammarImpl grammar = new GrammarImpl(new ExpressionPool());
    
    public static Grammar remove( AnnotatedGrammar src ) {
        AGMBuilder builder = new AGMBuilder(src);
        
        builder.grammar.setTopLevel(src.getTopLevel().visit(builder));
        builder.grammar.setPlugs( (GrammarImpl.Plug[]) builder.plugs.toArray(new GrammarImpl.Plug[0]) );
        
        return builder.grammar;
    }
    
    
    private AGMBuilder( AnnotatedGrammar grammar ) {
        super(new ExpressionPool());
        
        // build anyContent
        anyContent = new ReferenceExp("anyContent");
        anyContent.exp = pool.createZeroOrMore(
            pool.createChoice(
                new ElementPattern(NameClass.ALL,anyContent),
                pool.createChoice(
                    pool.createAttribute(NameClass.ALL),
                    Expression.anyString)));
        
        // build initial forest
        ClassItem[] ci = grammar.getClasses();
        for( int i=0; i<ci.length; i++ ) {
            if( ci[i].agm.exp==null )   ci[i].agm.exp=ci[i].exp; // UGLY
            class2agm.put( ci[i], new ReferenceExp(null,ci[i].agm) );
        }
                
        // connect those forests to each other
        for( int i=0; i<ci.length; i++ ) {
            ReferenceExp e = (ReferenceExp)class2agm.get(ci[i]);
             e.exp = e.exp.visit(this);
        }
    }
    
    public Expression onRef( ReferenceExp exp ) {
        Expression e = (Expression)ref2exp.get(exp);
        if( e!=null )   return e;

        e = exp.exp.visit(this);
        ref2exp.put(exp,e);
        
        return e;
    }
    
    public Expression onOther( OtherExp exp ) {
        if( exp instanceof ExternalItem ) {
            Expression e = ((ExternalItem)exp).createAGM(pool);
            if( e instanceof GrammarImpl.Plug )
                plugs.add(e);
            return e;
        }
        if( exp instanceof ClassItem ) {
            return (Expression)class2agm.get(exp);
        }
        // ignore all OtherExps
        return exp.exp.visit(this);
    }
    
    public Expression onAttribute( AttributeExp exp ) {
        return pool.createAttribute(exp.nameClass,exp.exp.visit(this));
    }
    
    public Expression onElement( ElementExp exp ) {
        ElementExp result = (ElementExp)elem2exp.get(exp);
        if(result==null) {
            result = grammar.createElement( exp.getNameClass(), Expression.nullSet );
            elem2exp.put(exp,result);
            result.contentModel = exp.getContentModel().visit(this);
            result.ignoreUndeclaredAttributes = exp.ignoreUndeclaredAttributes;
        }
        return result;
    }
}
