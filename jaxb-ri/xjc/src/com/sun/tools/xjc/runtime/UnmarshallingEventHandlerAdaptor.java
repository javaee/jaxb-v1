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
 * @(#)$Id: UnmarshallingEventHandlerAdaptor.java,v 1.2 2005-09-10 18:20:45 kohsuke Exp $
 */
package com.sun.tools.xjc.runtime;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Redirects events to another SAX ContentHandler.
 * 
 * <p>
 * Note that the SAXException returned by the ContentHandler is
 * unreported. So we have to catch them and report it, then rethrow
 * it if necessary. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class UnmarshallingEventHandlerAdaptor implements UnmarshallingEventHandler {
    
    protected final UnmarshallingContext context;

    /** This handler will receive SAX events. */
    protected final ContentHandler handler;

    public UnmarshallingEventHandlerAdaptor(UnmarshallingContext _ctxt,ContentHandler _handler) throws SAXException {
        this.context = _ctxt;
        this.handler = _handler;
        
        // emulate the start of documents
        try {
            handler.setDocumentLocator(context.getLocator());
            handler.startDocument();
            declarePrefixes( context.getAllDeclaredPrefixes() );
        } catch( SAXException e ) {
            error(e);
        }
    }

    public Object owner() {
        return null;
    }


    // nest level of elements.
    private int depth = 0;
        
    public void enterAttribute(String uri, String local, String qname) throws SAXException {
    }

    public void enterElement(String uri, String local, String qname, Attributes atts) throws SAXException {
        depth++;
        context.pushAttributes(atts,true);
        try {
            declarePrefixes(context.getNewlyDeclaredPrefixes());
            handler.startElement(uri,local,qname,atts);
        } catch( SAXException e ) {
            error(e);
        }
    }

    public void leaveAttribute(String uri, String local, String qname) throws SAXException {
    }

    public void leaveElement(String uri, String local, String qname) throws SAXException {
        try {
            handler.endElement(uri,local,qname);
            undeclarePrefixes(context.getNewlyDeclaredPrefixes());
        } catch( SAXException e ) {
            error(e);
        }
        context.popAttributes();
        
        depth--;
        if(depth==0) {
            // emulate the end of the document
            try {
                undeclarePrefixes(context.getAllDeclaredPrefixes());
                handler.endDocument();
            } catch( SAXException e ) {
                error(e);
            }
            context.popContentHandler();
        }
    }
    
    private void declarePrefixes( String[] prefixes ) throws SAXException {
        for( int i=prefixes.length-1; i>=0; i-- )
            handler.startPrefixMapping(
                prefixes[i],
                context.getNamespaceURI(prefixes[i]) );
    }
    
    private void undeclarePrefixes( String[] prefixes ) throws SAXException {
        for( int i=prefixes.length-1; i>=0; i-- )
            handler.endPrefixMapping( prefixes[i] );
    }

    public void text(String s) throws SAXException {
        try {
            handler.characters(s.toCharArray(),0,s.length());
        } catch( SAXException e ) {
            error(e);
        }
    }
    
    private void error( SAXException e ) throws SAXException {
        context.handleEvent(new ValidationEventImpl(
            ValidationEvent.ERROR,
            e.getMessage(),
            new ValidationEventLocatorImpl(context.getLocator()),
            e
        ), false);
    }

    public void leaveChild(int nextState) throws SAXException {
    }
}
