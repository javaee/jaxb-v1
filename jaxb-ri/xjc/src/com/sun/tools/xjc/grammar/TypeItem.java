/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar;

import org.xml.sax.Locator;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;

/**
 * JavaItem that outputs an object during the unmarshaling.
 * 
 * <p>
 * ClassItem, InterfaceItem and PrimitiveItem are derived classes
 * of this interface.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public abstract class TypeItem extends JavaItem {
    public TypeItem(String displayName, Locator loc) {
        super(displayName, loc);
    }

    /** gets the type of objects which will be created by this JavaItem. */
    public abstract JType getType();


    /**
     * Sort types in such an order that
     * if t[i] is a sub-type of t[j], then i>j.
     * 
     * IOW, this is a topological sort based on the derivation hierarchy. 
     */
    public static void sort( TypeItem[] t ) {
        
        for( int i=0; i<t.length-1; i++ ) {
            int k=i;
            JClass tk = toJClass(t[k]);
                        
            for( int j=i+1; j<t.length; j++ ) {
                JClass tj = toJClass(t[j]);
                if( tk.isAssignableFrom(tj) ) {
                    k = j;
                    tk = tj;
                }
            }
            
            // swap t[i] and t[k]
            TypeItem tmp = t[i];
            t[i] = t[k];
            t[k] = tmp;
        }
    }
    
    private static JClass toJClass( TypeItem t ) {
        JType jt = t.getType();
        if( jt.isPrimitive() )  return ((JPrimitiveType)jt).getWrapperClass();
        else                    return (JClass)jt;
    }
    
    public String toString() {
        return getClass().getName()+'['+getType().fullName()+']';
    }
}
