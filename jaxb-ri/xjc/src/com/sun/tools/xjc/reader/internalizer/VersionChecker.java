/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.internalizer;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import com.sun.tools.xjc.reader.Const;

/**
 * Checks the jaxb:version attribute on a XML Schema document.
 * 
 * jaxb:version is optional if no binding customization is used,
 * but if present, its value must be "1.0".
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class VersionChecker extends XMLFilterImpl {

    /**
     * We store the value of the version attribute in this variable
     * when we hit the root element.
     */
    private String version = null ;
    
    /** Will be set to true once we hit the root element. */
    private boolean seenRoot = false;
    
    /** Will be set to true once we hit a binding declaration. */
    private boolean seenBindings = false;
    
    private Locator locator;
    
    /**
     * Stores the location of the start tag of the root tag.
     */
    private Locator rootTagStart;
    
    public VersionChecker( XMLReader parent ) {
        setParent(parent);
    }
    
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
        throws SAXException {
        
        super.startElement(namespaceURI, localName, qName, atts);
        
        if(!seenRoot) {
            // if this is the root element
            seenRoot = true;
            rootTagStart = new LocatorImpl(locator);
            
            version = atts.getValue(Const.JAXB_NSURI,"version");
            if( namespaceURI.equals(Const.JAXB_NSURI) ) {
                String version2 = atts.getValue("","version");
                if( version!=null && version2!=null ) {
                    // we have both @version and @jaxb:version. error.
                    SAXParseException e = new SAXParseException(
                        Messages.format( Messages.TWO_VERSION_ATTRIBUTES ), locator );
                    getErrorHandler().error(e);
                }
                if( version==null )
                    version = version2;
            }
            
        }
        
        if( Const.JAXB_NSURI.equals(namespaceURI) )
            seenBindings = true;
    }

    public void endDocument() throws SAXException {
        super.endDocument();
        
        if( seenBindings && version==null ) {
            // if we see a binding declaration but not version attribute
            SAXParseException e = new SAXParseException(
                Messages.format(Messages.ERR_VERSION_NOT_FOUND),rootTagStart);
            getErrorHandler().error(e);
        }
        
        // if present, the value must be 1.0
        if( version!=null && !version.equals(Const.JAXB_VERSION) ) {
            SAXParseException e = new SAXParseException(
                Messages.format(Messages.ERR_INCORRECT_VERSION),rootTagStart);
            getErrorHandler().error(e);
        }
    }
    
    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        this.locator = locator;
    }

}
