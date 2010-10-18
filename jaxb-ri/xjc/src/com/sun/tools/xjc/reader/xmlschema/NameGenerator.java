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

package com.sun.tools.xjc.reader.xmlschema;

import java.text.ParseException;

import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSTermVisitor;

/**
 * Utility functions that are related to name computations.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class NameGenerator {
    
    /**
     * Computes a name from unnamed model group by following the spec.
     * 
     * Taking first three elements and combine them.
     * 
     * @exception ParseException
     *      If the method cannot generate a name. For example, when
     *      a model group doesn't contain any element reference/declaration
     *      at all.
     */
    static public String getName( final BGMBuilder builder, XSModelGroup mg )
        throws ParseException {

        final StringBuffer name = new StringBuffer();
        
        mg.visit(new XSTermVisitor() {
            /**
             * Count the number of tokens we combined.
             * We will concat up to 3.
             */
            private int count=0;
            
            public void wildcard(XSWildcard wc) {
                append("any");
            }

            public void modelGroupDecl(XSModelGroupDecl mgd) {
                modelGroup(mgd.getModelGroup());
            }

            public void modelGroup(XSModelGroup mg) {
                String operator;
                if(mg.getCompositor()==XSModelGroup.CHOICE)     operator = "Or";
                else                                            operator = "And";
                
                int size = mg.getSize();
                for( int i=0; i<size; i++ ) {
                    mg.getChild(i).getTerm().visit(this);
                    if(count==3)    return; // we have enough
                    if(i!=size-1)   name.append(operator);
                }
            }

            public void elementDecl(XSElementDecl ed) {
                append(ed.getName());
            }
            
            private void append(String token) {
                if( count<3 ) {
                    name.append(
                        builder.getNameConverter().toClassName(token));
                    count++;
                }
            }
        });
        
        if(name.length()==0) throw new ParseException("no element",-1);
        
        return name.toString();
    }
}
