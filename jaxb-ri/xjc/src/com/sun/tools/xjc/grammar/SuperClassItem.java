/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar;

import org.xml.sax.Locator;

import com.sun.msv.grammar.Expression;

/**
 * represents a derivation relationship between two classes.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class SuperClassItem extends JavaItem {
    public SuperClassItem( Expression exp, Locator loc ) {
        super("superClass-marker",loc);
        this.exp=exp;
    }
    
    /** actual super class definition. */
    public ClassItem definition = null;

    public Object visitJI( JavaItemVisitor visitor ) {
        return visitor.onSuper(this);
    }
}
