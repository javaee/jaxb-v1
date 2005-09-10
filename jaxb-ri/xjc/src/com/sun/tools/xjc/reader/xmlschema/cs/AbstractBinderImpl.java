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
package com.sun.tools.xjc.reader.xmlschema.cs;

import com.sun.codemodel.JDefinedClass;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSSchema;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration;

/**
 * A set of helper methods to make it easy to implement
 * {@link ClassBinder}-derived class.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
abstract class AbstractBinderImpl implements ClassBinder {
    
    protected final BGMBuilder builder;
    protected final ClassSelector owner;
    
    protected AbstractBinderImpl( ClassSelector _owner ) {
        this.owner = _owner;
        this.builder = owner.builder;
    }
    
    
    
    /** Wraps a CodeModel class into a ClassItem object. */
    protected final ClassItem wrapByClassItem( XSComponent sc, JDefinedClass cls ) {

        final ClassItem ci = owner.builder.grammar.createClassItem(cls,Expression.epsilon,sc.getLocator());

        final XSAnnotation annon = sc.getAnnotation();
        if(annon!=null) {
            final BindInfo bi = (BindInfo)annon.getAnnotation();
            if(bi!=null) {
                final BIDeclaration[] decls = bi.getDecls();
                for (int index = 0; index < decls.length; index++) {
                    ci.declarations.add(decls[index]);
                }
            }
        }
        return ci;
    }

    /**
     * Derives a name from a schema component.
     * Use the name of the schema component as the default name.
     */
    protected final String deriveName( XSDeclaration comp ) {
        return deriveName( comp.getName(), comp );
    }

    /**
     * Derives a name from a schema component.
     * 
     * This method handles prefix/suffix modification and 
     * XML-to-Java name conversion.
     * 
     * @param name
     *      The base name. This should be things like element names
     *      or type names.
     * @param comp
     *      The component from which the base name was taken.
     *      Used to determine how names are modified.
     */
    protected final String deriveName( String name, XSComponent comp ) {
        XSSchema owner = comp.getOwnerSchema();
        
        if( owner!=null ) {
            BISchemaBinding sb = (BISchemaBinding)builder.getBindInfo(
                owner).get(BISchemaBinding.NAME);
            
            if(sb!=null)    name = sb.mangleClassName(name,comp);
        }
        
        name = builder.getNameConverter().toClassName(name);
        
        return name;
    }
    
    protected static void _assert( boolean b ) {
        if(!b)
            throw new JAXBAssertionError();
    }
}
