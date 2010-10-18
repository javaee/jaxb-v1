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

package com.sun.tools.xjc.reader.dtd.bindinfo;

import java.util.HashMap;
import java.util.StringTokenizer;

import org.dom4j.Element;

import com.sun.msv.datatype.xsd.NmtokenType;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.grammar.xducer.EnumerationXducer;
import com.sun.tools.xjc.grammar.xducer.Transducer;

/**
 * &lt;enumeration> declaration in the binding file.
 */
public final class BIEnumeration implements BIConversion
{
    /** Creates an object from &lt;enumeration> declaration. */
    private BIEnumeration( Element _e, Transducer _xducer ) {
        this.e = _e;
        this.xducer = _xducer;
    }
    
    /** &lt;enumeration> element in DOM. */
    private final Element e;
    
    private final Transducer xducer;
    
    public String name() { return e.attributeValue("name"); }
    
    /** Returns a transducer for this enumeration declaration. */
    public Transducer getTransducer() { return xducer; }
    
    
    
    
    /** Creates a global enumeration declaration. */
    static BIEnumeration create( Element dom, BindInfo parent ) {
        // create a class in the target package.
        return new BIEnumeration(
            dom,
            new EnumerationXducer(
                parent.nameConverter,
                parent.classFactory.createClass(
                    parent.getTargetPackage(),
                    dom.attributeValue("name"),
                    null ), // TODO: error reporting support
                buildMemberExp(dom),
                emptyHashMap,
                null/*TODO:location support*/));
    }
    
    /** Creates an element-local enumeration declaration. */
    static BIEnumeration create( Element dom, BIElement parent ) {
        // create a class as a nested class
        return new BIEnumeration(
            dom,
            new EnumerationXducer(
                parent.parent.nameConverter,
                parent.parent.classFactory.createClass(
                    parent.getClassObject(),
                    dom.attributeValue("name"),
                    null ), // TODO: error reporting support
                buildMemberExp(dom),
                emptyHashMap,
                null/*TODO:location support*/ ));
    }
    
    private static final HashMap emptyHashMap = new HashMap();
    
    private static Expression buildMemberExp( Element dom ) {
        String members = dom.attributeValue("members");
        if(members==null) members="";   // TODO: error handling
        
        ExpressionPool pool = new ExpressionPool();
        
        Expression exp = Expression.nullSet;
        StringTokenizer tokens = new StringTokenizer(members);
        while(tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            
            exp = pool.createChoice( exp,
                pool.createValue(NmtokenType.theInstance,token));
        }
        
        return exp;
    }
}
