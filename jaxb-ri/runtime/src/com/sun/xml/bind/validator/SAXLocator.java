/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.validator;

import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;

import org.xml.sax.SAXParseException;

/**
 * @since JAXB1.0
 */
public class SAXLocator implements Locator {
    
    /**
     * Set the appropriate locator information on the supplied
     * ValidationEvent.
     */
    public ValidationEventLocator getLocation( SAXParseException saxException ) {
        return new ValidationEventLocatorImpl(saxException);
    }
    
}
