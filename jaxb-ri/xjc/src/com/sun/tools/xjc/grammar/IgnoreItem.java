/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar;

import org.xml.sax.Locator;

import com.sun.msv.grammar.Expression;

/**
 * Used to mark the ignored part of the grammar.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class IgnoreItem extends JavaItem {
    public IgnoreItem(Locator loc) {
        super("$ignore", loc);
    }

    public IgnoreItem(Expression exp, Locator loc) {
        this(loc);
        this.exp = exp;
    }
    public Object visitJI(JavaItemVisitor visitor) {
        return visitor.onIgnore(this);
    }
}
