/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;

import java.io.OutputStream;
import java.io.IOException;

/**
 * Represents a resource file in the application-specific file format.
 */
public abstract class JResourceFile {

    private final String name;
    
    protected JResourceFile( String name ) {
        this.name = name;
    }
    
    /**
     * Gets the name of this property file
     */
    public String name() { 
        return name; 
    }
    
    /**
     * called by JPackage to produce the file image.
     */
    protected abstract void build( OutputStream os ) throws IOException;
}
