/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
