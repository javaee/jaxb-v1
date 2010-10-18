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
