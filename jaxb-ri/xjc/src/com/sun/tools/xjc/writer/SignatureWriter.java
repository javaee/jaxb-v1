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

package com.sun.tools.xjc.writer;

import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldUse;

/**
 * Dumps an annotated grammar in a simple format that
 * makes signature check easy.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SignatureWriter {
    
    public static void write( AnnotatedGrammar grammar, Writer out )
        throws IOException {
        
        new SignatureWriter(grammar,out).dump();
    }
    
    private SignatureWriter( AnnotatedGrammar grammar, Writer out ) {
        this.out = out;
        this.classes = grammar.getClasses();
        
        for( int i=0; i<classes.length; i++ )
            classSet.put( classes[i].getTypeAsDefined(), classes[i] );
    }
    
    /** All the ClassItems in this grammar. */
    private final ClassItem[] classes;
    /** Map from content interfaces to ClassItem. */
    private final Hashtable classSet = new Hashtable();
    
    private final Writer out;
    private int indent=0;
    private void printIndent() throws IOException {
        for( int i=0; i<indent; i++ )
            out.write("  ");
    }
    private void println(String s) throws IOException {
        printIndent();
        out.write(s);
        out.write('\n');
    }
    
    private void dump() throws IOException {
        
        // collect packages used in the class.
        Set packages = new TreeSet(new Comparator() {
            public int compare(Object lhs, Object rhs) {
                return ((JPackage)lhs).name().compareTo(
                    ((JPackage)rhs).name());
            }
        });
        for( int i=0; i<classes.length; i++ ) {
            JDefinedClass cls = classes[i].getTypeAsDefined();
            packages.add(cls._package());
        }
        
        for (Iterator itr = packages.iterator(); itr.hasNext();)
            dump( (JPackage) itr.next() );
        
        out.flush();
    }
    
    private void dump( JPackage pkg ) throws IOException {
        println("package "+pkg.name()+" {");
        indent++;
        dumpChildren(pkg);
        indent--;
        println("}");
    }
    
    private void dumpChildren( JClassContainer cont ) throws IOException {
        Iterator itr = cont.classes();
        while(itr.hasNext()) {
            JDefinedClass cls = (JDefinedClass)itr.next();
            ClassItem ci = (ClassItem)classSet.get(cls);
            if(ci!=null)
                dump(ci);
        }
    }
    
    private void dump( ClassItem ci ) throws IOException {
        JDefinedClass cls = ci.getTypeAsDefined();
        
        StringBuffer buf = new StringBuffer();
        buf.append("interface ");
        buf.append(cls.name());
        
        boolean first=true;
        Iterator itr = cls._implements();
        while(itr.hasNext()) {
            if(first) {
                buf.append(" extends ");
                first=false;
            } else {
                buf.append(", ");
            }
            buf.append( printName((JClass)itr.next()) );
        }
        buf.append(" {");
        println(buf.toString());
        indent++;
        
        // dump the field
        FieldUse[] fu = ci.getDeclaredFieldUses();
        for( int i=0; i<fu.length; i++ ) {
            String type;
            if(!fu[i].multiplicity.isAtMostOnce())
                type = "List<"+printName(fu[i].type)+">";
            else
                type = printName(fu[i].type);
            println(type+" "+fu[i].name+";");
        }
        
        dumpChildren(cls);
        
        indent--;
        println("}");
    }
    
    /** Get the display name of a type. */
    private String printName( JType t ) {
        String name = t.fullName();
        if(name.startsWith("java.lang."))
            name = name.substring(10);  // chop off the package name
        return name;
    }
}
