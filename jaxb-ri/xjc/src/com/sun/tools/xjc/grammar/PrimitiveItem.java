/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar;

import org.xml.sax.Locator;

import com.sun.codemodel.JType;
import com.sun.msv.datatype.DatabindableDatatype;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.xducer.Transducer;

/**
 * Represents a "primitive" item.
 * 
 * <p>
 * This item causes a transformation from a string to an object.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class PrimitiveItem extends TypeItem {

    protected PrimitiveItem( Transducer _xducer, DatabindableDatatype _guard, Expression _exp, Locator loc ) {
        // name is used only for debug purpose.
        super(_xducer.toString(),loc);
        
        this.xducer = _xducer;
        this.exp = _exp;
        this.guard = _guard;
    }
    
    public JType getType() { return xducer.getReturnType(); }
    
    /** Transducer that handles actual conversion. */
    public final Transducer xducer;

    /**
     * Datatype object that defines text that can be accepted by this
     * PrimitiveItem.
     */
    public final DatabindableDatatype guard;
    
    public Object visitJI( JavaItemVisitor visitor ) {
        return visitor.onPrimitive(this);
    }
}
