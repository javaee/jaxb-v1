/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
