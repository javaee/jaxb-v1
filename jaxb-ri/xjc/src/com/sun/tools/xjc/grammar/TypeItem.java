/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
