/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
