/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.validator;

import javax.xml.bind.ValidationEventLocator;

import org.xml.sax.SAXParseException;

/**
 * @since JAXB1.0
 */
public interface Locator {
 
    /**
     * Get the appropriate locator information on 
     */
    public ValidationEventLocator getLocation( SAXParseException saxException );
    
}
