/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.util;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.InterleaveExp;
import com.sun.msv.grammar.ListExp;
import com.sun.msv.grammar.SequenceExp;
import com.sun.msv.grammar.util.ExpressionFinder;

/**
 * Returns true if the expression accepts an input string of length > 1.
 * 
 * Basically we just look for interleaves and sequences.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class GroupFinder extends ExpressionFinder {
    
    private GroupFinder() {}    // no instanciation please
    
    private static final ExpressionFinder theInstance = new GroupFinder();
    
    /** Public entry point. */
    public static boolean find( Expression e ) {
        return e.visit(theInstance);
    }
    
// terminals
    public boolean onAttribute(AttributeExp exp) {
        return false;
    }

    public boolean onElement(ElementExp exp) {
        return false;
    }

    public boolean onList(ListExp exp) {
        return false;
    }

// things that we are looking for
    public boolean onInterleave(InterleaveExp exp) {
        return true;
    }

    public boolean onSequence(SequenceExp exp) {
        return true;
    }
}
