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
