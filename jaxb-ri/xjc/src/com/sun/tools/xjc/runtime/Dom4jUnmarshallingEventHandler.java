/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)$Id: Dom4jUnmarshallingEventHandler.java,v 1.1 2004-06-25 21:15:21 kohsuke Exp $
 */
package com.sun.tools.xjc.runtime;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * {@link UnmarshallingEventHandler} implementation for dom4j.
 * See http://www.dom4j.org/ for details.
 * 
 * @optionalRuntime
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Dom4jUnmarshallingEventHandler extends UnmarshallingEventHandlerAdaptor {
    private Element owner;
    
    public Dom4jUnmarshallingEventHandler(UnmarshallingContext _ctxt) throws SAXException {
        super(_ctxt, new SAXContentHandler(new DocumentFactory()));
    }
    
    public void enterElement(String uri, String local, String qname, Attributes atts) throws SAXException {
        super.enterElement(uri, local, qname, atts);
        if( owner==null )
            owner = ((SAXContentHandler)handler).getDocument().getRootElement();
    }

    public Object owner() {
        return owner;
    }
    public Element getOwner() {
        return owner;
    }
}
