/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xjc.reader.dtd.bindinfo;

import org.dom4j.Element;
import org.dom4j.QName;
import org.xml.sax.Locator;

class DOM4JLocator
{
    private static final String locationNamespace =
        "http://www.sun.com/xmlns/jaxb/dom4j-location";
    private static final String systemId    = "systemid";
    private static final String column      = "column";
    private static final String line        = "line";
    
    /** Sets the location information to a specified element. */
    public static void setLocationInfo( Element e, Locator loc ) {
        e.addAttribute(
            QName.get(systemId,locationNamespace),
            loc.getSystemId());
        e.addAttribute(
            QName.get(column,  locationNamespace),
            Integer.toString(loc.getLineNumber()));
        e.addAttribute(
            QName.get(line,    locationNamespace),
            Integer.toString(loc.getColumnNumber()));
    }
    
    /**
     * Gets the location information from an element.
     * 
     * <p>
     * For this method to work, the setLocationInfo method has to be
     * called before.
     */
    public static Locator getLocationInfo( final Element e ) {
        if(e.attribute(QName.get(systemId,locationNamespace))==null)
            return null;    // no location information
        
        return new Locator(){
            public int getLineNumber() {
                return Integer.parseInt(e.attributeValue(
                    QName.get(line,locationNamespace)));
            }
            public int getColumnNumber() {
                return Integer.parseInt(e.attributeValue(
                    QName.get(column,locationNamespace)));
            }
            public String getSystemId() {
                return e.attributeValue(
                    QName.get(systemId,locationNamespace));
            }
            // we are not interested in PUBLIC ID.
            public String getPublicId() { return null; }
        };
    }
}
