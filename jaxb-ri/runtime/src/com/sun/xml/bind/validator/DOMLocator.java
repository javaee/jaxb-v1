/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.validator;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;

import org.xml.sax.SAXParseException;

import com.sun.xml.bind.unmarshaller.DOMScanner;


/**
 * @since JAXB1.0
 */
public class DOMLocator implements Locator {
    
    private DOMScanner scanner = null;
    
    public DOMLocator( DOMScanner scanner ) {
        this.scanner = scanner;
    }
    
    /**
     * Set the appropriate locator information on the supplied
     * ValidationEvent.
     */
    public ValidationEventLocator getLocation( SAXParseException saxException ) {
                                                   
        ValidationEventLocatorImpl vel = new ValidationEventLocatorImpl();
        
        try {
            vel.setURL( new URL( saxException.getSystemId() ) );
        } catch( MalformedURLException me ) {
            vel.setURL( null );
        }
        // get the location from the scanner and set it on the locator
        vel.setNode( scanner.getCurrentLocation() );
        return vel;
    }
    
}
