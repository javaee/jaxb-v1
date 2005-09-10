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

import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.FieldUse;

/**
 * Counts the multiplicity of a given field item.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class FieldMultiplicityCounter extends MultiplicityCounter {
    /**
     * @param _name
     *      the name of the {@link FieldItem} to count.
     */
    private FieldMultiplicityCounter( String _name ) {
        this.name = _name;
    }
    
    public static Multiplicity count( Expression exp, FieldItem fi ) {
        return (Multiplicity)exp.visit(
            new FieldMultiplicityCounter(fi.name));
    }
    
    public static Multiplicity count( Expression exp, FieldUse fu ) {
        return (Multiplicity)exp.visit(
            new FieldMultiplicityCounter(fu.name));
    }
    
    /** Name of the field to be counted. */
    private final String name;

    protected Multiplicity isChild(Expression exp) {
        if(exp instanceof FieldItem) {
            FieldItem fi = (FieldItem)exp;
            if(fi.name.equals(name))    return fi.multiplicity;
            else                        return Multiplicity.zero;
        } else
            return null;
    }

}
