/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator;

import java.util.Iterator;

import com.sun.codemodel.JType;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.TypeItem;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class JavadocBuilder {
    
    /**
     * Returns a string that represents a list of possible
     * objects that can be in the given field.
     */
    public static String listPossibleTypes( FieldUse fu ) {
        StringBuffer buf = new StringBuffer();
        
        for( Iterator itr=fu.items.iterator(); itr.hasNext(); ) {
            FieldItem fi = (FieldItem)itr.next();
            TypeItem[] types = fi.listTypes();
            for( int i=0; i<types.length; i++ ) {
                JType t = types[i].getType();
                if( t.isPrimitive() || t.isArray() )
                    buf.append(t.fullName());
                else
                    buf.append("{@link "+t.fullName()+"}\n");
            }
        }
        
        return buf.toString();
    }
}
