/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema;
import org.xml.sax.Locator;

import com.sun.codemodel.JDefinedClass;

/**
 * Encapsulate where a class is created.
 */
public interface JClassFactory {
    /**
     * Create a new JClass object with the given name.
     * 
     * The actual location where the class is created will be
     * determined by the callee, not by the caller.
     * 
     * @param sourceLocation
     *      This location is recorded as the owner of the new class.
     *      Used to report errors if necessary.
     */
    JDefinedClass create( String name, Locator sourceLocation );
    
    /**
     * Get the parent factory.
     */
    JClassFactory getParentFactory();
}