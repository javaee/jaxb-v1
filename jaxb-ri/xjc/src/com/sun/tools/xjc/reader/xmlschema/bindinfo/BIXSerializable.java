/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;


/**
 * This customization will enable serialization support on XJC.
 * This is used as a child of a {@link BIGlobalBinding} object,
 * and this doesn't implement BIDeclaration by itself.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class BIXSerializable {

    /** serial version UID. */
    private final long uid;
    
    public BIXSerializable( long _uid ) {
        uid = _uid;
    }
    
    /** Gets the serial version UID to be used. */
    public long getUID() { return uid; }
}
