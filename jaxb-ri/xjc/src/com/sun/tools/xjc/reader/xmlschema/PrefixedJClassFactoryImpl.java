/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema;
import org.xml.sax.Locator;

import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.xml.xsom.XSModelGroupDecl;

// TODO: should be moved to the cs subpackage.

/**
 * JClassFactory that cretes a new class by using
 * an existing class name as a prefix (instead of
 * nesting them as inner classes)
 */
public class PrefixedJClassFactoryImpl implements JClassFactory {
    /** Parent context. */
    private final JClassFactory parent;
    
    /** Prefix to prepend. */
    private final String prefix;
    
    /** Package to which a new class should be created. */
    private final JClassContainer pkg;
    
    private final BGMBuilder builder;
    
    /**
     * This implementation is primarily for model group declarations.
     * This constructor makes it easy to instanciate a new
     * PrefixedJClassFactoryImpl for a model group impl.
     */
    public PrefixedJClassFactoryImpl( BGMBuilder builder, JDefinedClass parentClass ) {
        this.builder = builder;
        this.parent = builder.selector.getClassFactory();
        this.prefix = parentClass.name();
        
        this.pkg = parentClass.parentContainer();
    }

    public PrefixedJClassFactoryImpl( BGMBuilder builder, XSModelGroupDecl decl ) {
        if( decl.isLocal() )
            throw new IllegalArgumentException();   // assertion failure
        
        this.builder = builder;
        this.parent = builder.selector.getClassFactory();
        this.prefix = builder.getNameConverter().toClassName(decl.getName());
        
        this.pkg = builder.selector.getPackage( decl.getTargetNamespace() );
    }
    
    public JDefinedClass create( String name, Locator sourceLocation ) {
        return builder.selector.codeModelClassFactory.createInterface(
            pkg,
            prefix + name,
            sourceLocation );
    }

    public JClassFactory getParentFactory() {
        return parent;
    }
}