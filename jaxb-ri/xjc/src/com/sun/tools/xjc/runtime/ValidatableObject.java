/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.runtime;

import com.sun.msv.verifier.DocumentDeclaration;

/**
 * This interface is implemented by generated classes
 * to indicate that the class supports validation.
 */
public interface ValidatableObject extends XMLSerializable
{
    /** Gets the schema fragment associated with this class. */
    DocumentDeclaration createRawValidator();
    
    /**
     * Gets the main interface that this object implements.
     * 
     * For example, <code>FooImpl</code> will return <code>Foo</code>
     * from this method.
     */
    Class getPrimaryInterface();
}
