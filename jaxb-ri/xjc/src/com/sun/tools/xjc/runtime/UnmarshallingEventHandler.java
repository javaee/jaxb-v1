/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)$Id: UnmarshallingEventHandler.java,v 1.1 2004-06-25 21:15:25 kohsuke Exp $
 */
package com.sun.tools.xjc.runtime;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Implemented by the generated code to unmarshall an object
 * from unmarshaller events.
 * 
 * <p>
 * AbstractUnmarshallingEventHandlerImpl throws a SAXException when a problem is encountered
 * and that problem is not reported. It is the responsibility of the caller
 * of this interface to report it to the client's ValidationEventHandler
 * and re-wrap it into UnmarshalException.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface UnmarshallingEventHandler {
    
    /**
     * Returns the content-tree object for which this unmarshaller
     * is working for.
     */
    Object owner();

    //
    // event handlers
    //
    void enterElement(String uri, String local, String qname, Attributes atts) throws SAXException;
    void leaveElement(String uri, String local, String qname ) throws SAXException;
    void text(String s) throws SAXException;
    void enterAttribute(String uri, String local, String qname ) throws SAXException;
    void leaveAttribute(String uri, String local, String qname ) throws SAXException;
    void leaveChild(int nextState) throws SAXException;
}