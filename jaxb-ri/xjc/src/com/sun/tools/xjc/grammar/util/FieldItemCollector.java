/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar.util;

import java.util.Hashtable;

import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.BGMWalker;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.xml.bind.JAXBAssertionError;

    
/**
 * Collects all FieldItem object below the given expression.
 * 
 * It is an assertion failure to see a TypeItem.
 */
public final class FieldItemCollector extends BGMWalker
{
    private final Hashtable m = new Hashtable();
    
    private FieldItemCollector() {}
    
    /**
     * Public entry point.
     */
    public static FieldItem[] collect( Expression exp ) {
        FieldItemCollector fim = new FieldItemCollector();
        exp.visit(fim);
        return (FieldItem[]) fim.m.values().toArray(
            new FieldItem[fim.m.values().size()]);
    }
    
    public Object onSuper(SuperClassItem item) {
        // because of the xsi:nil support, we now have expression like
        // <element name="foo">
        //   <choice>
        //     <field name="nil">
        //       <attribute name="xsi:nil"> ... </attribute>
        //     </field>
        //     <super-class>
        //       <class-ref name="x"/>
        //     </super-class>
        //   </choice>
        // </element>
        //
        // we need to think more about the implication of this.
        return null;
            
        // we will never see SuperClassItem, since
        // SuperClassItem cannot be under choice or oneOrMore
        // (since it must be multiplicity (1,1)
//            throw new InternalError();
    }
    public Object onField(FieldItem item) {
        m.put(item.name,item);
        return null;
    }
    public Object onIgnore(IgnoreItem item) { return null; }
        
        
    // we are expecting FieldItem. we can never possibly see
    // TypeItems
    public Object onClass(ClassItem item)           { throw new JAXBAssertionError(); }
    public Object onInterface(InterfaceItem item)   { throw new JAXBAssertionError(); }
    public Object onPrimitive(PrimitiveItem item)   { throw new JAXBAssertionError(); }
    public Object onExternal(ExternalItem item)     { throw new JAXBAssertionError(); }
        
}