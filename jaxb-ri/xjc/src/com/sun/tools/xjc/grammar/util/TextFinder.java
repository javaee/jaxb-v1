/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.util;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.DataExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ListExp;
import com.sun.msv.grammar.MixedExp;
import com.sun.msv.grammar.ValueExp;
import com.sun.msv.grammar.util.ExpressionFinder;

/**
 * A function object that returns true if an expression contains a text
 * in its top-level. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class TextFinder extends ExpressionFinder {
    /** Public entry point. */
    public static boolean find( Expression e ) {
        return e.visit(theInstance);
    }
    
    private static final ExpressionFinder theInstance = new TextFinder();
    
    private TextFinder() {}
    
// negative terminals
    public boolean onAttribute(AttributeExp exp) {
        return false;
    }
    public boolean onElement(ElementExp exp) {
        return false;
    }
// positive terminals
    public boolean onAnyString() {
        return true;
    }
    public boolean onData(DataExp exp) {
        return true;
    }
    public boolean onList(ListExp exp) {
        return true;
    }
    public boolean onMixed(MixedExp exp) {
        return true;
    }
    public boolean onValue(ValueExp exp) {
        return true;
    }
}
