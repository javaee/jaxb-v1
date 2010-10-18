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

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import com.sun.codemodel.JDefinedClass;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.BGMWalker;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.JavaItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.tools.xjc.util.Util;

/**
 * Annotate the type inheritance relationship among generated classes
 * by finds all S-C, C-S, I-C, and I-I relationships in the grammar.
 */
public class HierarchyAnnotator
{
    /** Set to a valid object (such as System.err) to obtain trace log. */
    private static PrintStream debug =
        Util.getSystemProperty(HierarchyAnnotator.class,"debug")!=null?System.out:null;

    public static void annotate(
        final AnnotatedGrammar grammar,
        final AnnotatorController controller ) {
        
        BGMWalker annotator = new BGMWalker() {
            
            private JavaItem parent = null;
            
            private final Set visitedExps = new HashSet();
            
            int indent = 0;
            
            private void printIndent() {
                for( int i=0; i<indent; i++ )
                    debug.print(' ');
            }
            
            
            /**
             * Recursively process children of 'exp' if necessary.
             */
            private void processChildren( JavaItem exp ) {
                if(!visitedExps.add(exp))   return;
                
                if(debug!=null) {
                    printIndent(); indent++;
                    debug.println("in "+exp);
                }
                
                JavaItem old = parent;
                parent = exp;
                exp.exp.visit(this);
                parent = old;

                if(debug!=null) {
                    indent--; printIndent();
                    debug.println("out "+exp);
                }
            }
            
            public Object onClass( ClassItem ci ) {
                if(parent instanceof SuperClassItem)
                    setSuperClassBody( (SuperClassItem)parent, ci );
                
                if(parent instanceof InterfaceItem)
                    setImplementedInterface(ci, (InterfaceItem)parent);
                
                processChildren(ci);
                return null;    // unused
            }
            
            public Object onInterface( InterfaceItem ii ) {
                if(parent instanceof InterfaceItem)
                    setImplementedInterface(ii, (InterfaceItem)parent);
                
                processChildren(ii);
                return null;    // unused
            }
            
            public Object onSuper( SuperClassItem si ) {
                if(parent instanceof ClassItem)
                    setSuperClassForClass( (ClassItem)parent, si );
                
                processChildren(si);
                
                if(parent instanceof ClassItem)
                    // reflect the relationship to the code model
                    ((ClassItem)parent).getTypeAsDefined()._implements(
                        si.definition.getTypeAsDefined() );
                
                return null;    // unused
            }
            
            public Object onField( FieldItem fi ) {
                // no need to go down
                return null;    // unused
            }
            
            public Object onPrimitive( PrimitiveItem pi ) {
                // no need to go down
                return null;    // unused
            }
            
            public Object onExternal( ExternalItem exp ) {
                // no more binding objects underneath. back up.
                return null;    // unused
            }
            
            public Object onIgnore( IgnoreItem ii ) {
                return null; // no need to visit inside
            }
        
            /**
             * this method is called when interface-class/interface-interface relationship
             * is found, and before the descendants of "class" is processed.
             */
            protected void setImplementedInterface( TypeItem c, InterfaceItem i ) {
                // type item must be a generated type.
                // (otherwise, we cannot make that type to implement "i")
                ((JDefinedClass)c.getType())._implements(i.getTypeAsClass());
            }
        
            /**
             * this method is called when class-super relationship is found, and after
             * all the descendants of "super" is processed.
             */
            protected void setSuperClassForClass( ClassItem p, SuperClassItem c ) {
                if( p.superClass!=null ) {
                    // this parent item already has a super class.
                    controller.reportError(
                        new Expression[]{
                            p,    // parent class item
                            p.superClass,    // previous super class definition.
                            c},    // newly found super class definition.
                        Messages.format(Messages.ERR_MULTIPLE_INHERITANCE,
                            new Object[]{p.name}));
                    return;
                }
                
                p.superClass = c;
            }
        
        
            /**
             * this method is called when super-class relationship is found, and before
             * the descendants of "class" is processed.
             */
            protected void setSuperClassBody( SuperClassItem parent, ClassItem child ) {
                // set the definition field of SuperClassItem.
                if( parent.definition!=null ) {
                    /* two definitions are found. This happens for patterns like
                        <group t:role="superClass">
                            <group t:role="class">
                                ....
                            </group>
                            <group t:role="class">
                                ....
                            </group>
                        </group>
                    */
                    controller.reportError(
                        new Expression[]{parent,child,parent.definition},
                        Messages.format(Messages.ERR_MULTIPLE_SUPERCLASS_BODY) );
                    // recover by ignoring this definition.
                    return;
                }

                parent.definition = child;
            }
        };
            
        // explicitly process all classes as some of them might be unreachable
        // from the root.
        ClassItem[] cs = grammar.getClasses();
        for( int i=0; i<cs.length; i++ )
            cs[i].visit(annotator);
        InterfaceItem[] is = grammar.getInterfaces();
        for( int i=0; i<is.length; i++ )
            is[i].visit(annotator);
    }


}
