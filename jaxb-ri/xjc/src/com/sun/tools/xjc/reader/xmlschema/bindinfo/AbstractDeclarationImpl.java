/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import org.xml.sax.Locator;

import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSComponent;

/**
 * Abstract partial implementation of {@link BIDeclaration}
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
abstract class AbstractDeclarationImpl implements BIDeclaration {

    protected AbstractDeclarationImpl( Locator _loc ) {
        this.loc = _loc;
    }
    
    private final Locator loc;
    public Locator getLocation() { return loc; }
    
    protected BindInfo parent;
    public void setParent(BindInfo p) { this.parent=p; }

    protected final XSComponent getOwner() {
        return parent.getOwner();
    }
    protected final BGMBuilder getBuilder() {
        return parent.getBuilder();
    }
    
    private boolean isAcknowledged = false;
    
    public final boolean isAcknowledged() { return isAcknowledged; }
    public void markAsAcknowledged() {
        isAcknowledged = true;
    }
    
    protected final static void _assert( boolean b ) {
        if(!b)
            throw new JAXBAssertionError();
    }
}
