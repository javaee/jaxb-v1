/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar;

import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.util.ExpressionWalker;

/**
 * A base class for visitor classes that can recognize BGM constructs.
 */
public abstract class BGMWalker extends ExpressionWalker implements JavaItemVisitor
{
    public void onOther( OtherExp exp ) {
        if(exp instanceof JavaItem)
            ((JavaItem)exp).visitJI(this);
        else
            exp.exp.visit(this);
    }
    
    public Object onClass(ClassItem item) {
        item.exp.visit(this);
        return null;
    }

    public Object onField(FieldItem item) {
        item.exp.visit(this);
        return null;
    }

    public Object onIgnore(IgnoreItem item) {
        item.exp.visit(this);
        return null;
    }

    public Object onInterface(InterfaceItem item) {
        item.exp.visit(this);
        return null;
    }

    public Object onPrimitive(PrimitiveItem item) {
        item.exp.visit(this);
        return null;
    }

    public Object onSuper(SuperClassItem item) {
        item.exp.visit(this);
        return null;
    }

    public Object onExternal(ExternalItem item) {
        item.exp.visit(this);
        return null;
    }
}
