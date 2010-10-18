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

import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.MixedExp;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;

/**
 * Replaces {@link MixedExp} with interlave.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class MixedRemover extends ExpressionCloner {
    
    /**
     * this set keeps the visited expressions, to prevent
     * infinite recursion.
     */
    private final Set visitedExps = new java.util.HashSet();
    
    
    private final AnnotatedGrammar grammar;
    
    public MixedRemover( AnnotatedGrammar g) {
        super(g.getPool());
        this.grammar = g;
    }
    
    public Expression onRef( ReferenceExp exp ) {
        if( visitedExps.add(exp) )
            exp.exp = exp.exp.visit(this);
        
        return exp;
    }
    
    public Expression onOther( OtherExp exp ) {
        if( visitedExps.add(exp) ) {
            exp.exp = exp.exp.visit(this);
        }
        
        return exp;
    }
    
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
    
    public Expression onMixed( MixedExp exp ) {
        /*
        expand this <mixed> as
        <interleave>
            <zeroOrMore>
                <primitiveItem>
                    <data type="string"/>
                </primitiveItem>
            </zeroOrMore>
            exp.exp
        </interleave>
        */
        return pool.createInterleave(
                pool.createZeroOrMore(
                    grammar.createPrimitiveItem(
                        new IdentityTransducer(grammar.codeModel),
                        StringType.theInstance, // no guard
                        pool.createData(StringType.theInstance),
                        null )),
                exp.exp.visit(this) );
    }
}
