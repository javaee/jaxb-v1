/*
 * @(#)$Id: XMLEntityResolverImpl.java,v 1.1 2004-06-25 21:15:20 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tools.xjc.reader.xmlschema.parser;

import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

/**
 * Wraps {@link EntityResolver}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class XMLEntityResolverImpl implements XMLEntityResolver {
    
    private final EntityResolver entityResolver;

    XMLEntityResolverImpl( EntityResolver er ) {
        if(er==null)    throw new NullPointerException();
        this.entityResolver = er;
    }
    
    public XMLInputSource resolveEntity(XMLResourceIdentifier r) throws XNIException, IOException {
        String publicId = r.getPublicId();
        String systemId = r.getExpandedSystemId();
        
        if(publicId==null)
            // perhaps this is for resolving a schema document.
            // I know this is an abuse of the public ID, but I claim
            // it's still better than not providing any entity resolution
            // support at all.
            //
            // this at least allows users to resolve
            // <xs:import> without @schemaLocation
            publicId = r.getNamespace();
        
        try {
            InputSource is = entityResolver.resolveEntity(publicId, systemId);
            if(is==null)        return null;
            
            XMLInputSource xis = new XMLInputSource(
                is.getPublicId(),
                is.getSystemId(),
                r.getBaseSystemId(),
                is.getByteStream(),
                is.getEncoding());
            
            xis.setCharacterStream(is.getCharacterStream());
            return xis;
        } catch (SAXException e) {
            throw new XNIException(e);
        }
        
    }
}
