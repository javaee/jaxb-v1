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
