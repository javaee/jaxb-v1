/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.util;

import java.util.Vector;

import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.BGMWalker;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * A function object that collects all {@link TypeItem} object
 * below the given expression.
 * 
 * <p>
 * If the function hits {@link FieldItem} or {@link SuperClassItem},
 * the assertion fails.    
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class TypeItemCollector extends BGMWalker {
    private final Vector vec = new Vector();
    
    private TypeItemCollector() {
    }
    
    /**
     * public entry point.
     */
    public static TypeItem[] collect( Expression e ) {
        TypeItemCollector tic = new TypeItemCollector();
        e.visit(tic);
        return (TypeItem[])tic.vec.toArray(new TypeItem[tic.vec.size()]);
    }
    
    public Object onClass(ClassItem item) {
        vec.add(item);
        return null;
    }
    public Object onInterface(InterfaceItem item) {
        vec.add(item);
        return null;
    }
    public Object onPrimitive(PrimitiveItem item) {
        vec.add(item);
        return null;
    }
    public Object onExternal(ExternalItem item) {
        vec.add(item);
        return null;
    }
    public Object onSuper(SuperClassItem item) {
        // we are expecting TypeItem. we can never possibly see
        // this. (since SuperClassItem are not wrapped by FieldItems.)
        throw new JAXBAssertionError();
    }
    public Object onField(FieldItem item) {
        // we are expecting TypeItem. we can never possibly see
        // this. (since FieldItems don't nest.)
        throw new JAXBAssertionError();
    }
    public Object onIgnore(IgnoreItem item) { return null; }
}