/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.cs;
import org.xml.sax.Locator;

import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.reader.xmlschema.JClassFactory;

/**
 * JClassFactory that puts new class under a package or another class.
 */
class JClassFactoryImpl implements JClassFactory {
    
    private final ClassSelector owner;
    
    /** Parent context. */
    private final JClassFactory parent;
    
    private final JClassContainer container;
    
    JClassFactoryImpl( ClassSelector owner, JClassContainer _cont ) {
        this.parent = owner.getClassFactory();
        this.container = _cont;
        this.owner = owner;
    }
    
    public JDefinedClass create( String name, Locator sourceLocation ) {
        return owner.codeModelClassFactory.createInterface(
            container, name, sourceLocation );
    }

    public JClassFactory getParentFactory() {
        return parent;
    }
}