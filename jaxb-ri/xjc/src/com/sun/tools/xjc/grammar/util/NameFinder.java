/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
