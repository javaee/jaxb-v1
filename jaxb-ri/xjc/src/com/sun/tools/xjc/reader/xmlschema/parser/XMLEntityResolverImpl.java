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
