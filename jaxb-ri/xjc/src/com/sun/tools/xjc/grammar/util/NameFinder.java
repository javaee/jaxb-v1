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

import java.util.HashSet;
import java.util.Set;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ChoiceNameClass;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.util.ExpressionWalker;

/**
 * Looks for elements or attributes in the expression
 * and returns the name class that represents all the elements/attributes
 * in the given expression. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class NameFinder extends ExpressionWalker {
    public static NameClass findElement( Expression e ) {
        return find( e, new NameFinder() {
            public void onElement( ElementExp e ) {
                onName(e.getNameClass());
            }
        } );
    }
    public static NameClass findAttribute( Expression e ) {
        return find( e, new NameFinder() {
            public void onAttribute( AttributeExp e ) {
                onName(e.getNameClass());
            }
        } );
    }
    
    private static NameClass find( Expression e, NameFinder f ) {
        e.visit(f);
        if(f.nc==null)  return NameClass.NONE;
        else            return f.nc.simplify();
    }
    
    private NameClass nc = null;
    private final Set visited = new HashSet();
    
    protected NameFinder() {}
    
    protected void onName(NameClass child) {
        if(nc==null)    nc=child;
        else
            nc = new ChoiceNameClass(nc,child);
    }
    public void onRef( ReferenceExp exp ) {
        if( visited.add(exp) )
           super.onRef(exp);
    }

    public void onAttribute(AttributeExp exp) {
        return;
    }

    public void onElement(ElementExp exp) {
        return;
    }

}
