/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.cs;

import com.sun.codemodel.JDefinedClass;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSSchema;

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
        return owner.builder.grammar.createClassItem(cls,Expression.epsilon,sc.getLocator());
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
