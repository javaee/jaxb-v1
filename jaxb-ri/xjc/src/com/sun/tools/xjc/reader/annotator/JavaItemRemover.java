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
