/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar;

import org.xml.sax.Locator;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.Expression;

/**
 * Represents a generated interface.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class InterfaceItem extends TypeItem {
    
    protected InterfaceItem( JClass _type, Expression body, Locator loc ) {
        super(_type.name(),loc);
        this.type = _type;
        this.exp = body;
    }

    private final JClass type;
    public JType getType() { return type; }
    
    /**
     * Returns the same object as the getType method but
     * this method exploits the fact that the getType method
     * always return an object of the type JClass.
     */
    public JClass getTypeAsClass() { return type; }
    
    public ClassItem getSuperType() { return null; } // interfaces do not have the super type.

    public Object visitJI( JavaItemVisitor visitor ) {
        return visitor.onInterface(this);
    }
}
