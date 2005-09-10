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
