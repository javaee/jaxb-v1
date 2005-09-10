/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
