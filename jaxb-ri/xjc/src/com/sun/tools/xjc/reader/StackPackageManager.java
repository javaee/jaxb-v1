/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader;

import java.util.Stack;

import org.xml.sax.Attributes;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;

/**
 * This class adopts the stack model and reads the package
 * information from "jaxb:package" attribute.
 * 
 * The startElement and the endElement event must be called
 * for every such event.
 */
public class StackPackageManager implements PackageManager
{
    /**
     * @param   pkg
     *      The initial current package.
     */
    public StackPackageManager( JPackage pkg ) {
        this.codeModel = pkg.owner();
        stack.push(pkg);
    }
    
    private final JCodeModel codeModel;
    
    /**
     * Gets the "current" package.
     * 
     * Typically, classes or interfaces are generated
     * into the returned package.
     * 
     * @return
     *      Non-null valid JPackage object.
     */
    public final JPackage getCurrentPackage() { return (JPackage)stack.peek(); }
    
    
    
    private final Stack stack = new Stack();
    
    public final void startElement( Attributes atts ) {
        if( atts.getIndex(Const.JAXB_NSURI,"package")!=-1 ) {
            // handle "t:package" attribute here.
            String name = atts.getValue(Const.JAXB_NSURI,"package");
            stack.push( codeModel._package(name) );
        } else
            // continue using the current one
            stack.push(stack.peek());
    }
    public final void endElement() {
        stack.pop();
    }
}
