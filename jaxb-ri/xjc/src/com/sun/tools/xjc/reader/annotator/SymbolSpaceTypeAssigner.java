/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.annotator;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sun.codemodel.JType;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.BGMWalker;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.id.SymbolSpace;
import com.sun.tools.xjc.reader.TypeUtil;

/**
 * Computes a type for each symbol space.
 * 
 * The type of a symbol space is determined by types of all classes
 * that have ID data for that symbol space.
 */
public class SymbolSpaceTypeAssigner
{
    public static void assign( AnnotatedGrammar grammar, AnnotatorController controller ) {
        
        //
        // accumulate possible types for symbol spaces.
        //
        final Map applicableTypes = new java.util.HashMap();
        
        // process all class items.
        ClassItem[] classes = grammar.getClasses();
        for( int i=0; i<classes.length; i++ ) {
            final ClassItem ci = classes[i];
            ci.exp.visit(new BGMWalker(){
                public Object onSuper( SuperClassItem sci ) {
                    // if the super class S has ID, then the type of that
                    // symbol space cannot be more specific than S.
                    // and we'll eventually process S even if we skip
                    // it now. So there is no need to process it now.
                    return null;
                }
                public Object onExternal( ExternalItem ei ) {
                    // no more interesting stuff under this node.
                    return null;
                }
                public Object onClass( ClassItem ci ) {
                    // we are about to enter another class item. back off.
                    return null;
                }
                public Object onField( FieldItem item ) {
                    item.exp.visit(this);
                    return null;
                }
                public Object onIgnore( IgnoreItem item ) {
                    // the grammar beyond this point will be ignored.
                    // so it cannot contain PrimitiveItem
                    return null;
                }
                public Object onInterface( InterfaceItem item ) {
                    // we are about to enter an interface, so it can't contain
                    // PrimitiveItem. back off.
                    return null;
                }
                public Object onPrimitive( PrimitiveItem item ) {
                    if(item.xducer.isID()) {
                        // found IDTransducer. record the type of the parent.
                        SymbolSpace ss = item.xducer.getIDSymbolSpace();
                        Set types = (Set)applicableTypes.get(ss);
                        if(types==null)
                            applicableTypes.put(ss,types=new java.util.HashSet());
                        
                        types.add(ci.getType());
                    }
                    
                    // there is no need to process children.
                    return null;
                }
            });
        }
        
        
        //
        // compute the common type for symbol spaces.
        //
        Iterator itr = applicableTypes.entrySet().iterator();
        while(itr.hasNext()) {
            Map.Entry e = (Map.Entry)itr.next();
            
            ((SymbolSpace)e.getKey()).setType(
                TypeUtil.getCommonBaseType(
                    grammar.codeModel,
                    (JType[])((Set)e.getValue()).toArray(new JType[0])));
        }
    }
}
