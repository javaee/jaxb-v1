/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)$Id: W3CDOMUnmarshallingEventHandler.java,v 1.1 2004-06-25 21:15:26 kohsuke Exp $
 */
package com.sun.tools.xjc.runtime;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.sun.xml.bind.marshaller.SAX2DOMEx;

/**
 * {@link UnmarshallingEventHandler} implementation for W3C DOM.
 * 
 * @optionalRuntime
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class W3CDOMUnmarshallingEventHandler extends UnmarshallingEventHandlerAdaptor {
    private Element owner;
    
    public W3CDOMUnmarshallingEventHandler(UnmarshallingContext _ctxt) throws ParserConfigurationException, SAXException {
        super(_ctxt, new SAX2DOMEx());
    }
    
    public void enterElement(String uri, String local, String qname, Attributes atts) throws SAXException {
        super.enterElement(uri, local, qname, atts);
        if( owner==null )
            owner = ((SAX2DOMEx)handler).getCurrentElement();
    }

    public Object owner() {
        return owner;
    }
    public Element getOwner() {
        return owner;
    }
}
