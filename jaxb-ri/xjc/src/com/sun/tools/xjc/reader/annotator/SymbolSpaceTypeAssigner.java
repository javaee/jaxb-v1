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
