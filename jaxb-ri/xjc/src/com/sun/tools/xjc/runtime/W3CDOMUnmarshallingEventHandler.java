/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

/*
 * @(#)$Id: W3CDOMUnmarshallingEventHandler.java,v 1.2 2005-09-10 18:20:46 kohsuke Exp $
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
