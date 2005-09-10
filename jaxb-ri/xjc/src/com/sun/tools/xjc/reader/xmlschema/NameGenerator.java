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
